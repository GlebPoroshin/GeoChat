package com.geochat.backend.service

import com.geochat.backend.dto.AuthResponse
import com.geochat.backend.dto.UserDto
import com.geochat.backend.model.UserEntity
import com.geochat.backend.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    fun register(nickname: String, email: String, password: String): AuthResponse {
        if (userRepository.findByEmail(email) != null) {
            throw IllegalArgumentException("Email уже используется")
        }
        if (userRepository.findByNickname(nickname) != null) {
            throw IllegalArgumentException("Никнейм уже используется")
        }

        val user = userRepository.save(UserEntity(
            nickname = nickname,
            email = email,
            password = passwordEncoder.encode(password)
        ))

        val accessToken = jwtService.generateAccessToken(email)
        val refreshToken = jwtService.generateRefreshToken(email)

        return AuthResponse(
            userId = user.id!!,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun updatePassword(email: String, newPassword: String) {
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("User not found")
        val hashedPassword = passwordEncoder.encode(newPassword)
        userRepository.save(user.copy(password = hashedPassword))
    }

    private fun UserEntity.toDto() = UserDto(
        id = id,
        nickname = nickname,
        email = email,
        roles = roles.map { it.name }.toSet()
    )
}
