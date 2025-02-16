package com.geochat.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GeoChatApplication

fun main(args: Array<String>) {
	runApplication<GeoChatApplication>(*args)
}
