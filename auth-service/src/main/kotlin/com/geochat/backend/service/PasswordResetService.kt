package com.geochat.backend.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Service
class PasswordResetService(
    private val redisTemplate: StringRedisTemplate,
    private val userService: UserService,
    private val emailService: EmailService
) {

    fun generateResetCode(email: String) {
        val code = Random.nextInt(100000, 999999).toString()
        redisTemplate.opsForValue().set("reset_code:$email", code, 10, TimeUnit.MINUTES)
        emailService.sendResetCode(email, code)
    }

    fun verifyResetCode(email: String, code: String): Boolean {
        val storedCode = redisTemplate.opsForValue().get("reset_code:$email")
        return storedCode != null && storedCode == code
    }

    fun resetPassword(email: String, code: String, newPassword: String) {
        if (!verifyResetCode(email, code)) {
            throw IllegalArgumentException("Invalid or expired reset code")
        }

        userService.updatePassword(email, newPassword)
        redisTemplate.delete("reset_code:$email")
    }
}
