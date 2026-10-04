package com.github.diogocerqueiralima.identity.service.application.commands

import java.math.BigInteger

data class MarkBootstrapCertificateAsUsedCommand(val serialNumber: BigInteger)
