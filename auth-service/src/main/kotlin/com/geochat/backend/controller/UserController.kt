package com.geochat.backend.controller

import com.geochat.backend.model.UserEntity
import com.geochat.backend.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: UUID): ResponseEntity<UserEntity?> {
        val user = userService.getUserById(id)
        return if (user != null) ResponseEntity.ok(user) else ResponseEntity.notFound().build()
    }

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserEntity>> {
        return ResponseEntity.ok(userService.getAllUsers())
    }

    @PostMapping
    fun createUser(
        @RequestParam username: String,
        @RequestParam email: String,
        @RequestParam password: String
    ): ResponseEntity<UserEntity> {
        val newUser = userService.createUser(username, email, password)
        return ResponseEntity.ok(newUser)
    }
}
