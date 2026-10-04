package com.github.diogocerqueiralima.asset.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(value = ["com.github.diogocerqueiralima"])
class AssetServiceApplication

fun main(args: Array<String>) {
    runApplication<AssetServiceApplication>(*args)
}
