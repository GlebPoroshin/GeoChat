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
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import org.springframework.context.annotation.Import
import com.geochat.backend.config.SecurityConfig
import com.geochat.backend.AuthServiceApplication

/**
 * Интеграционные тесты для [AuthController].
 * 
 * Тесты проверяют основные сценарии аутентификации и авторизации:
 * - Регистрация нового пользователя
 * - Вход в систему
 * - Обновление токена
 * - Выход из системы
 * - Сброс пароля
 *
 * Для тестов используется:
 * - H2 in-memory база данных
 * - Redis для хранения токенов и кодов сброса пароля
 * - MockMvc для моковых запросов
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [AuthServiceApplication::class])  // Явно указываем главный класс
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfig::class)  // Подключаем конфигурацию безопасности
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

    /**
     * Очищает тестовые данные перед каждым прогоном:
     * - Удаляет всех пользователей из БД
     * - Удаляет коды сброса пароля из Redis
     * - Удаляет refresh токены из Redis
     */
    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
        redisTemplate.delete("password-reset:$testEmail")
        redisTemplate.delete("refresh:$testEmail")
    }

    /**
     * Проверяет успешную регистрацию нового пользователя.
     * 
     * Ожидаемый результат:
     * - 200 OK
     * - В ответе приходят userId, accessToken и refreshToken
     */
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
    }

    /**
     * Проверяет ошибку регистрации, если email уже существует.
     * 
     * Ожидаемый результат:
     * - 400 Bad Request
     */
    @Test
    fun `should not register user with existing email`() {
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

    /**
     * Проверяет успешный вход в систему.
     * 
     * Ожидаемый результат:
     * - 200 OK
     * - В ответе прихдят userId, accessToken и refreshToken
     */
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

    /**
     * Проверяет успешное обновление токена.
     * 
     * Ожидаемый результат:
     * - 200 OK
     * - В ответе приходят userId, accessToken и refreshToken
     */
    @Test
    fun `should refresh token successfully`() {
        createTestUser()
        val refreshToken = loginAndGetRefreshToken()

        redisTemplate.opsForValue().set("refresh:$testEmail", refreshToken, 30, TimeUnit.DAYS)

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

    /**
     * Проверяет успешный выход из системы.
     * 
     * Ожидаемый результат:
     * - 200 OK
     */
    @Test
    fun `should logout successfully`() {
        createTestUser()
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

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/logout")
                .param("email", testEmail)
                .header("Authorization", "Bearer ${response.accessToken}")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    /**
     * Проверяет генерацию кода для сброса пароля.
     * 
     * Ожидаемый результат:
     * - 200 OK
     */
    @Test
    fun `should generate reset code`() {
        createTestUser()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/forgot-password")
                .param("email", testEmail)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    /**
     * Проверяет верификацию кода сброса пароля.
     * 
     * Ожидаемый результат:
     * - HTTP 200 OK
     * - Ответ содержит "true"
     */
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

    /**
     * Проверяет процесс сброса пароля.
     * 
     * Шаги:
     * - Успешная смена пароля
     * - Невозможность входа со старым паролем
     * - Возможность входа с новым паролем
     * 
     * Ожидаемый результат:
     * - 200 OK при сбросе пароля
     * - 401 Unauthorized при попытке входа со старым паролем
     * - 200 OK при входе с новым паролем
     */
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

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/login")
                .param("email", testEmail)
                .param("password", testPassword) // старый пароль
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/login")
                .param("email", testEmail)
                .param("password", "newPassword123")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    /**
     * Создает тестового пользователя в базе данных.
     * 
     * @return Созданный пользователь
     */
    private fun createTestUser() {
        val user = UserEntity(
            nickname = testNickname,
            email = testEmail,
            password = passwordEncoder.encode(testPassword)
        )
        userRepository.save(user)
    }

    /**
     * Выполняет вход и возвращает refresh token.
     * 
     * @return Refresh token для авторизованного пользователя
     */
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

    /**
     * Генерирует код сброса пароля и сохраняет его в Redis.
     * 
     * @return Сгенерированный код сброса пароля
     */
    private fun generateResetCode(): String {
        val code = Random.nextInt(100000, 999999).toString()
        redisTemplate.opsForValue().set("password-reset:$testEmail", code, 30, TimeUnit.MINUTES)
        return code
    }
}
