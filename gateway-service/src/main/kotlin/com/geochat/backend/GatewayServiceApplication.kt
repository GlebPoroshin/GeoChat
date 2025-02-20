package com.geochat.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class GatewayServiceApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<GatewayServiceApplication>(*args)
        }
    }
}
