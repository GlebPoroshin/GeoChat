package com.geochat.backend.service

import com.geochat.backend.dto.AuthResponse
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

    fun authenticate(email: String, password: String): AuthResponse {
        val authToken = UsernamePasswordAuthenticationToken(email, password)
        authenticationManager.authenticate(authToken)

        val user = userRepository.findByEmail(email) 
            ?: throw IllegalArgumentException("User not found")
        
        val accessToken = jwtService.generateAccessToken(email)
        val refreshToken = jwtService.generateRefreshToken(email)

        redisTemplate.opsForValue().set(email, refreshToken, 30, TimeUnit.DAYS)

        return AuthResponse(
            userId = user.id!!,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun refreshToken(email: String, refreshToken: String): AuthResponse {
        val storedToken = redisTemplate.opsForValue().get(email)
        if (storedToken == null || storedToken != refreshToken) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        if (!jwtService.validateToken(refreshToken)) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        val user = userRepository.findByEmail(email) 
            ?: throw IllegalArgumentException("User not found")
        
        val newAccessToken = jwtService.generateAccessToken(email)

        return AuthResponse(
            userId = user.id!!,
            accessToken = newAccessToken,
            refreshToken = refreshToken
        )
    }

    fun logout(email: String) {
        redisTemplate.delete(email)
    }
}
