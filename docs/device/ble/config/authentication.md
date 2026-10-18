# Authentication Service

The Authentication Service exposes the device's authentication configuration over BLE. This document lists its UUIDs, types and allowed actions. For what each characteristic means and its default value, refer to the [Authentication Configuration](../../config/authentication.md). The rules a client sees when it reads the CSR, installs a certificate or revokes the credentials are described in [Operations by state](../../security/authentication/credentials.md#operations-by-state).

## Structure

**Service UUID**: `347df573-cf50-f1b5-e749-1efe2d71272e`

| Name | UUID | Type | Actions |
|---|---|---|---|
| csr | 90785634-12ef-cd2b-9008-f6e5d4c3b2a1 | file | read |
| certificate | df5c268d-107b-d836-e808-a442ac515561 | file | read, write |
| ca | 39897685-53c9-7092-d542-7dbfc95f7dce | file | read, write |
| revoke | 6e5b0cbc-87f6-9c9a-4d43-e29bb72441aa | boolean | write |
| status | aeb2f94c-b465-568d-264f-8a4ca22abc62 | string | read |

## Error Codes

An operation that the device [refuses](../../security/authentication/credentials.md#operations-by-state) because of its state fails with ATT error `0x13` (`Value Not Allowed`). This covers a `csr` read, a `certificate` write and a `revoke` write in a state that does not allow them, and a `certificate` write with an invalid certificate.
