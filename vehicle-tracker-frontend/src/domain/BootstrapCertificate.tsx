export interface BootstrapCertificate {

    serialNumber: string
    subject: string
    issuedAt: Date
    expiresAt: Date
    revoked: boolean
    used: boolean

}