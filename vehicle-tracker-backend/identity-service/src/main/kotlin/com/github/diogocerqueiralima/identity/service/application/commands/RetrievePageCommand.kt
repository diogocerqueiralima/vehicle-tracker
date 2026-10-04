package com.github.diogocerqueiralima.identity.service.application.commands

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class RetrievePageCommand(

    @get:Min(value = 1, message = "page number should be greater than or equal to 1")
    val page: Int,

    @get:Min(value = 1, message = "page size should be greater than or equal to 1")
    @get:Max(value = 50, message = "page size should be less than or equal 50")
    val pageSize: Int

)
