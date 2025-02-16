package com.geochat.backend.service

import com.geochat.backend.model.UserEntity
import com.geochat.backend.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun register(email: String, password: String): UserEntity {
        if (userRepository.findByEmail(email) != null) {
            throw IllegalArgumentException("Email уже используется")
        }

        val user = UserEntity(
            email = email,
            password = passwordEncoder.encode(password)
        )

        return userRepository.save(user)
    }

    fun findByEmail(email: String): UserEntity? {
        return userRepository.findByEmail(email)
    }

    fun updatePassword(email: String, newPassword: String) {
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("User not found")
        val hashedPassword = passwordEncoder.encode(newPassword)
        userRepository.save(user.copy(password = hashedPassword))
    }
}
