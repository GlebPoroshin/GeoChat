package com.geochat.backend.service

import com.geochat.backend.model.UserEntity
import com.geochat.backend.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(private val userRepository: UserRepository) {

    fun getUserById(id: UUID): UserEntity? {
        return userRepository.findById(id).orElse(null)
    }

    fun getUserByUsername(username: String): UserEntity? {
        return userRepository.findByUsername(username)
    }

    fun createUser(username: String, email: String, password: String): UserEntity {
        val user = UserEntity(
            username = username,
            email = email,
            password = password // В следующем шаге будем хешировать пароль
        )
        return userRepository.save(user)
    }

    fun getAllUsers(): List<UserEntity> {
        return userRepository.findAll()
    }
}
