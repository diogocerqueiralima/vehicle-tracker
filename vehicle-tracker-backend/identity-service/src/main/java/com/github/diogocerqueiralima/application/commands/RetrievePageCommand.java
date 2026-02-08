package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RetrievePageCommand(

        @Min(value = 1, message = "page number should be greater or equal to 1")
        int page,

        @Min(value = 1, message = "page size should be greater or equal to 1")
        @Max(value = 50, message = "page size should be lower or equal 50")
        int pageSize

) {}
