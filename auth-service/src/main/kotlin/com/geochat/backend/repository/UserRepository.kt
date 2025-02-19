package com.geochat.backend.repository

import com.geochat.backend.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByNickname(nickname: String): UserEntity?
    fun findByEmail(email: String): UserEntity?
}
