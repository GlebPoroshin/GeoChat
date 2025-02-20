package com.geochat.backend.filter

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

@Component
class JwtAuthenticationFilter(
    @Value("\${jwt.secret}") private val secretKey: String
) : GlobalFilter, Ordered {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val path = request.path.value()

        if (path.startsWith("/auth-service/api/auth/") ||
            path.startsWith("/auth-service/swagger-ui/") ||
            path.startsWith("/auth-service/v3/api-docs") ||
            path.contains("swagger-ui.html")
        ) {
            return chain.filter(exchange)
        }

        val authHeader = request.headers.getFirst("Authorization")
        val startOfToken = "Bearer "

        if (authHeader == null || !authHeader.startsWith(startOfToken)) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            return exchange.response.setComplete()
        }

        val token = authHeader.split(startOfToken)[1]
        return try {
            validateToken(token)
            chain.filter(exchange)
        } catch (e: Exception) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            exchange.response.setComplete()
        }
    }

    private fun validateToken(token: String) {
        val key = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))
        val claims: Claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        if (claims.subject.isNullOrBlank()) {
            throw RuntimeException("Invalid JWT token: missing subject")
        }
    }

    override fun getOrder(): Int = -1
} 
