package com.geochat.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AuthServiceApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<AuthServiceApplication>(*args)
        }
    }
}
