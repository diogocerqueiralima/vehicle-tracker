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
Without the Bootstrap Certificate, the device cannot request a new Certificate, which means that the device will be permanently blocked from authenticating with the other services. This is a trade-off between security and usability, and it will be discussed in more detail in the [Certificate Revocation](#certificate-revocation) section.

## Certificate Lifecycle

The **Certificate** is a long-term certificate that is used for the authentication of the device with other services, such as the MQTT broker. It is issued by the Identity Service when the device requests it using the Bootstrap Certificate. The Certificate has a longer validity period, typically several months, to allow the device to authenticate with the other services without needing to request a new Certificate frequently.

The lifecycle of the Certificate can be summarized in the following steps:

1. The device uses the Bootstrap Certificate to request a Certificate from the Identity Service.
2. The Identity Service issues a Certificate to the device.
3. The device uses the Certificate to authenticate with other services, such as the MQTT broker.
4. The Certificate should be renewed before it expires to ensure uninterrupted authentication with the other services.

The following diagram illustrates the Certificate lifecycle:
<figure style="text-align:center">
  <img src="images/certificate-lifecycle.svg" alt="Certificate Lifecycle">
  <figcaption>Figure 2: Certificate Lifecycle</figcaption>
</figure>

The Certificate lifecycle has two important aspects: **renewal** and **revocation**. These aspects are crucial for maintaining the security of the system, and they will be discussed in the following sections.

## Certificate Renewal

As mentioned earlier, the Certificate has a longer validity period, typically several months. However, it is important to renew the Certificate before it expires to ensure uninterrupted authentication with the other services. The renewal process involves the following steps:

1. The device detects that the Certificate is about to expire.
2. The device uses the old Certificate to request a new Certificate from the Identity Service.
3. The Identity Service issues a new Certificate to the device.
4. The device starts using the new Certificate for authentication with the other services and the old Certificate is no longer valid.

So, what happens if the device fails to renew the Certificate before it expires? In this case, the device will not be able to authenticate with the other services, which means that it will be effectively blocked from accessing the system. This is similar to the scenario where the device is compromised and the user revokes the Certificate, which will be discussed in the next section.

The device can fail to renew the Certificate for many reasons, such as network issues, power failure, etc. Therefore, it is important to have a mechanism in place to handle such scenarios, such as sending notifications to the user when the Certificate is about to expire and with this, the user can try to resolve the issue, such as checking the network connection or power supply, to ensure that the device can renew the Certificate successfully.

## Certificate Revocation

Certificate revocation is the process of invalidating a Certificate, this can be done by the user or just happens when the Certificate expires. When a Certificate is revoked, it can no longer be used for authentication with the other services, which means that the device associated with the revoked Certificate will be effectively blocked from accessing the system.

So, how the device can have a new Certificate if the previous one is revoked? The answer is simple: the user should request to the administrator to issue a new Bootstrap Certificate and then install it on the device, which will allow the device to request a new Certificate from the Identity Service. This approach is a trade-off between security and usability, as it adds an extra layer of security to the system by preventing the device from requesting a new Certificate on its own, but it also means that the device will be temporarily blocked from accessing the system until the user requests a new Bootstrap Certificate.

The user could request the Bootstrap Certificate for the device without the need to ask the administrator, but this approach has a security risk, because if the key of the Bootstrap Certificate is not stored securely on the device, it could be compromised and used by an attacker.

In the future, can be built a user-friendly interface with the device, such as a web interface, that allows the user to request a new Bootstrap Certificate for the device without the need to ask the administrator, but this interface should be designed with security in mind to prevent unauthorized access and misuse.

It is important to note that certificate revocation is not a common scenario. In practice, a certificate is typically revoked when the device is compromised or when it reaches its expiration date. Due to the relative rarity and difficulty of such situations occurring, this approach was chosen as a balanced security measure, ensuring stronger protection without significantly impacting normal system usage.