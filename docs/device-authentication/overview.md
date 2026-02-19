# Device Authentication Overview

Devices are an important part of the vehicle tracking system. They are responsible for sending the location data to the server, which is then used to track the vehicle. To ensure that only authorized devices can send data to the server, we need to implement device authentication.

The devices send their data to the MQTT broker, which is responsible for authenticating the devices before allowing them to publish their data. This is possible by verifying if the certificate provided by the device is valid and has been issued by a trusted certificate authority (CA). The CA is responsible for issuing certificates to the devices, which can be used for authentication.

The system designed for device authentication is called the "Identity Service". The Identity Service is responsible for issuing and managing the certificates for the devices. We will talk more about the Identity Service in the [Identity Service](./identity-service.md) section. However, the main idea is that the Identity Service will issue **Bootstrap Certificates** to the devices, which will be used for the device requesting a **Certificate** from the Identity Service. The **Certificate** will then be used for authenticating the device with the MQTT broker.

The device authentication process can be summarized in the following steps:

1. The administrator requests a Bootstrap Certificate for the Device from the Identity Service.
2. The Identity Service issues a Bootstrap Certificate to the Device.
3. The Device uses the Bootstrap Certificate to request a Certificate from the Identity Service.
4. The Identity Service issues a Certificate to the Device.
5. The Device uses the Certificate to authenticate with the MQTT broker and publish its data.

The following diagram illustrates the device authentication process:

<figure style="text-align:center">
  <img src="images/device-authentication-flow.svg" alt="Device Authentication Flow">
  <figcaption>Figure 1: Device Authentication Flow</figcaption>
</figure>

There are some important points to note about the device authentication process:

- The Bootstrap Certificate is a temporary certificate that is used only for the initial authentication of the device. It has a short validity period and is not used for regular authentication with the MQTT broker.
- The Certificate issued by the Identity Service is a long-term certificate that is used for regular authentication
- What if the private key of the device is compromised? In this case, the user should be able to revoke the certificate associated with the device, which will prevent the compromised device from authenticating with the MQTT broker. The Identity Service should have a mechanism to revoke certificates, such as a Certificate Revocation List (CRL) or an Online Certificate Status Protocol (OCSP) responder. This question will be answered in the [Certificate Lifecycle](./certificate-lifecycle.md) section.
- The Identity Service should also have a mechanism to renew certificates before they expire. This can be done by allowing devices to request a new certificate before the old one expires, or by automatically renewing certificates that are about to expire. The renewal process should be automated to ensure that devices do not lose connectivity due to expired certificates.

Summarizing, device authentication is a crucial aspect of the vehicle tracking system to ensure that only authorized devices can send data to the server. The Identity Service plays a key role in managing the certificates for the devices and ensuring secure authentication with the MQTT broker. You can find more details about the Identity Service in the [Identity Service](./identity-service.md) section.