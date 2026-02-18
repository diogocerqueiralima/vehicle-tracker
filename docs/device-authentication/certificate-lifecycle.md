# Certificate Lifecycle

There is two types of certificates in the system: the **Bootstrap Certificate** and the **Certificate**. The two types of certificates have different lifecycles, which are described in the following sections.

## Bootstrap Certificate Lifecycle

The **Bootstrap Certificate** is a temporary certificate that is used only once for the initial authentication of the device. It is issued by the Identity Service when the administrator requests it for a device.

The Bootstrap Certificate has a short validity period, typically a few days, to ensure that it is not used for any malicious purposes.
The lifecycle of the Bootstrap Certificate can be summarized in the following steps:

1. The administrator requests a Bootstrap Certificate for the Device from the Identity Service.
2. The Identity Service issues a Bootstrap Certificate to the Device.
3. The Device uses the Bootstrap Certificate to request a Certificate from the Identity Service.
4. The Bootstrap Certificate is no longer valid and cannot be used for authentication.
5. With the Certificate issued by the Identity Service, the device can authenticate with other services, such as the MQTT broker, until the Certificate expires or is revoked.

The following diagram illustrates the Bootstrap Certificate lifecycle:

<figure style="text-align:center">
  <img src="images/bootstrap-certificate-lifecycle.svg" alt="Bootstrap Certificate Lifecycle">
  <figcaption>Figure 1: Bootstrap Certificate Lifecycle</figcaption>
</figure>

There is one important point to note about the Bootstrap Certificate lifecycle: **only** the administrator can request a Bootstrap Certificate for a device. This means that the device cannot request a Bootstrap Certificate on its own, which adds an extra layer of security to the system. So, if the device is compromised, the user can simply revoke the Certificate associated with the device, which will prevent the compromised device from authenticating with the other services, but then how the user can request a new Bootstrap Certificate to the device?
Because, without the Bootstrap Certificate, the device cannot request a new Certificate, which means that the device will be permanently blocked from authenticating with the other services. This is a trade-off between security and usability, and it will be discussed in more detail in the [Certificate Revocation](#certificate-revocation) section.

### Bootstrap Certificate Installation

The Bootstrap Certificate is installed on the device by the administrator. The administrator should generate a **Certificate Signing Request (CSR)** and submit it to the Identity Service to request a Bootstrap Certificate for the device. The Identity Service will then issue a Bootstrap Certificate and provide it to the administrator, who will then install it on the device.

The installation process will need to copy the Bootstrap Certificate and the corresponding private key to the device. However, it is important to ensure that the private key is securely stored on the device and is not accessible to unauthorized users. The administrator should follow best practices for secure key management, such as using a secure storage mechanism and restricting access to the private key.

## Certificate Lifecycle

## Certificate Renewal

## Certificate Revocation