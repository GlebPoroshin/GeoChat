package com.geochat.backend.service

import com.geochat.backend.repository.UserRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
    private val redisTemplate: StringRedisTemplate
) {

    fun authenticate(email: String, password: String): Map<String, String> {
        val authToken = UsernamePasswordAuthenticationToken(email, password)
        authenticationManager.authenticate(authToken)

        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("User not found")
        val accessToken = jwtService.generateAccessToken(user)
        val refreshToken = jwtService.generateRefreshToken(user)

        redisTemplate.opsForValue().set(user.email, refreshToken, 30, TimeUnit.DAYS)

        return mapOf("accessToken" to accessToken, "refreshToken" to refreshToken)
    }

    fun refreshToken(email: String, refreshToken: String): Map<String, String> {
        val storedToken = redisTemplate.opsForValue().get(email)

        if (storedToken == null || storedToken != refreshToken) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("User not found")

        if (!jwtService.validateToken(refreshToken, email)) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        val newAccessToken = jwtService.generateAccessToken(user)

        return mapOf("accessToken" to newAccessToken)
    }

    fun logout(email: String) {
        redisTemplate.delete(email)
    }
}
