export interface BootstrapCertificateDTO {

    serial_number: string
    subject: string
    issued_at: Date
    expires_at: Date
    revoked: boolean
    used: boolean

}