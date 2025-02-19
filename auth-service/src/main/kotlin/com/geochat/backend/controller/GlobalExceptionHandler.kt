package com.geochat.backend.controller

import com.geochat.backend.dto.exceptions.EmailAlreadyExistsException
import com.geochat.backend.dto.exceptions.NickNameAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleEmailAlreadyExists(ex: EmailAlreadyExistsException): Map<String, String> {
        return mapOf("error" to ex.message!!)
    }

    @ExceptionHandler(NickNameAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleNickNameAlreadyExists(ex: NickNameAlreadyExistsException): Map<String, String> {
        return mapOf("error" to ex.message!!)
    }
}
