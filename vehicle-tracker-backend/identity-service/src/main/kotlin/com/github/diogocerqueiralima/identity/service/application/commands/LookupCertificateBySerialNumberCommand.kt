package com.github.diogocerqueiralima.identity.service.application.commands

import java.math.BigInteger

data class LookupCertificateBySerialNumberCommand(val serialNumber: BigInteger)
