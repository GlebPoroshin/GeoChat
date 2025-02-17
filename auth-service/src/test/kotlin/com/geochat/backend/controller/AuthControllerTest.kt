package com.geochat.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.geochat.backend.dto.AuthResponse
import com.geochat.backend.model.UserEntity
import com.geochat.backend.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val redisTemplate: StringRedisTemplate
) {
    private val testEmail = "test@email.com"
    private val testPassword = "password123"
    private val testNickname = "testUser"

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
        redisTemplate.delete(testEmail)
    }

    @Test
    fun `should register a new user`() {
        val requestBody = mapOf(
            "email" to testEmail,
            "password" to testPassword,
            "nickname" to testNickname
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.userId").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").exists())
            .andReturn()
    }

    @Test
    fun `should not register user with existing email`() {
        // Создаем первого пользователя
        createTestUser()

        val requestBody = mapOf(
            "email" to testEmail,
            "password" to "anotherPassword",
            "nickname" to "anotherNickname"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should login user successfully`() {
        createTestUser()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/login")
                .param("email", testEmail)
                .param("password", testPassword)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.userId").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").exists())
    }

    @Test
    fun `should refresh token successfully`() {
        createTestUser()
        val refreshToken = loginAndGetRefreshToken()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/refresh")
                .param("email", testEmail)
                .param("refreshToken", refreshToken)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.userId").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").exists())
    }

    @Test
    fun `should logout successfully`() {
        createTestUser()
        loginAndGetRefreshToken()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/logout")
                .param("email", testEmail)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `should generate reset code`() {
        createTestUser()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/forgot-password")
                .param("email", testEmail)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `should verify reset code`() {
        createTestUser()
        val code = generateResetCode()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/verify-code")
                .param("email", testEmail)
                .param("code", code)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("true"))
    }

    @Test
    fun `should reset password`() {
        createTestUser()
        val code = generateResetCode()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/reset-password")
                .param("email", testEmail)
                .param("code", code)
                .param("newPassword", "newPassword123")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    private fun createTestUser() {
        val user = UserEntity(
            nickname = testNickname,
            email = testEmail,
            password = passwordEncoder.encode(testPassword)
        )
        userRepository.save(user)
    }

    private fun loginAndGetRefreshToken(): String {
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/login")
                .param("email", testEmail)
                .param("password", testPassword)
        )
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            AuthResponse::class.java
        )
        return response.refreshToken
    }

    private fun generateResetCode(): String {
        return "123456"
    }
}
