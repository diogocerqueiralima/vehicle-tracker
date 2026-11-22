# TPM and the Identity Service CA Key

This document explains what a TPM is, why the `identity-service` CA key lives in one, and how the `tpm` Ansible role sets it up.

## 1. What is a TPM

A **TPM (Trusted Platform Module)** is a small security chip, or a virtual equivalent, that generates and stores cryptographic keys and uses them on behalf of software.

The key property: a key created **inside** the TPM can sign data but **cannot be read out**. Software sends data to the TPM, the TPM signs it and returns only the signature. Stealing the host's files does not give an attacker the private key.

The VPS needs a TPM 2.0, physical or virtual. On Linux it is exposed as:

| Device | Purpose |
|---|---|
| `/dev/tpm0` | Raw device, one client at a time |
| `/dev/tpmrm0` | Through the kernel resource manager, shareable. This is the one we use |

Both are owned by `tss:tss` with mode `0660`, so a user needs to be in the `tss` group to use them.

## 2. Why we use it

The `identity-service` acts as a certificate authority and signs device certificates (`SHA256withECDSA`). The CA private key must never leak, so it is generated in the TPM and the service only asks the TPM to sign.

Only the public certificate (`identity-service.crt`) is written to the host.

## 3. How the service reaches the key: PKCS#11

Java signs with hardware keys through **PKCS#11**, a standard interface used by HSMs and smartcards. The `identity-service` loads a PKCS#11 keystore (`KeyStoreConfig`, using `SunPKCS11`).

**tpm2-pkcs11** is the bridge between PKCS#11 and the TPM:

```
identity-service (Java, SunPKCS11)
        │  PKCS#11
        ▼
libtpm2_pkcs11.so
        │  TPM2 commands
        ▼
    /dev/tpmrm0  →  TPM
```

### Store, token and PINs

tpm2-pkcs11 keeps a **store** (a SQLite database) on disk, with one or more **tokens**. A token is a virtual smartcard that holds keys and certificates, and has two PINs:

| PIN | Used by | For |
|---|---|---|
| **SO PIN** (Security Officer) | An administrator | Managing the token, resetting the user PIN. Only needed when creating the token |
| **User PIN** | The `identity-service` (`PKCS11_PIN`) | Logging in to the token and using the keys |

### What is, and is not, on the host

- **Never on the host:** the plaintext private key. The TPM generates it.
- **On the host:** the store, which holds a blob of the key **wrapped by the TPM**. It is useless on another machine, because only this TPM can unwrap it.
- Using the key through tpm2-pkcs11 requires the **user PIN**: the auth values of the keys are wrapped by a key that is sealed in the TPM and only unlocked with the PIN. Someone with the store and `/dev/tpmrm0` but without the PIN cannot sign this way. Anyone who has the PIN (the service receives it as `PKCS11_PIN`) can, so the PIN must be kept secret and be strong. The key itself can never be extracted.

If the TPM is cleared or replaced, the keys are gone and a new key and certificate must be created.

## 4. The `tpm` Ansible role

The role runs from `playbooks/site.yml`, after `common` and `podman`. It is idempotent: it creates nothing that already exists.

| Step | What it does |
|---|---|
| Checks | Fails if `TPM_SO_PIN` or `TPM_USER_PIN` is unset, or if `/dev/tpmrm0` does not exist |
| Packages | `libtpm2-pkcs11-tools` (`tpm2_ptool`, which pulls `tpm2-tools`), `libtpm2-pkcs11-1`, `pkcs11-provider`, `openssl` |
| Access | Adds the `deploy` user to the `tss` group |
| Store | Creates `/home/deploy/.tpm2_pkcs11` (`0700`) and runs `tpm2_ptool init` |
| Token | `tpm2_ptool addtoken` with label `IDENTITY_SERVICE_CA` |
| Key | `tpm2_ptool addkey --algorithm=ecc256`, generated inside the TPM (label `identity-service-ca-key`) |
| Certificate | `openssl req -x509` with the `pkcs11` provider, so the TPM signs it |
| Token + export | `tpm2_ptool addcert` adds the certificate to the token, and a copy is written to `tls_cert_dir` |

Everything that touches the store runs as `deploy`, the user that runs the service, so the store belongs to it.

### Variables (`inventory/production/group_vars/all.yml`)

| Variable | Meaning |
|---|---|
| `tpm_store_dir` | Where the store lives on the VPS |
| `tpm_token_label` | Token label (`IDENTITY_SERVICE_CA`) |
| `tpm_key_label` | Key label, also the certificate label |
| `tls_cert_dir` | Where the certificate is exported (`/etc/tracker/certs`) |
| `tpm_ca_subject`, `tpm_ca_validity_days` | Certificate subject and validity |
| `tpm_so_pin`, `tpm_user_pin` | Read from the `TPM_SO_PIN` and `TPM_USER_PIN` environment variables |

The PINs are secrets and must never be committed.