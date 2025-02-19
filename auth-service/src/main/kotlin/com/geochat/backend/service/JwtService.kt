package com.geochat.backend.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*

@Service
class JwtService {

    @Value("\${jwt.secret}")
    lateinit var secret: String

    @Value("\${jwt.access-token-expiration}")
    var accessTokenExpiration: Long = 0

    @Value("\${jwt.refresh-token-expiration}")
    var refreshTokenExpiration: Long = 0

    fun generateAccessToken(email: String): String {
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + accessTokenExpiration))
            .signWith(getSignKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun generateRefreshToken(email: String): String {
        return generateToken(email, refreshTokenExpiration)
    }

    private fun generateToken(email: String, expiration: Long): String {
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(getSignKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = extractClaims(token)
            claims.expiration.after(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun extractEmail(token: String): String? {
        return extractClaims(token).subject
    }

    private fun extractClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(getSignKey())
            .build()
            .parseClaimsJws(token)
            .body
    }

    private fun getSignKey(): Key {
        val keyBytes = Decoders.BASE64.decode(secret)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}
