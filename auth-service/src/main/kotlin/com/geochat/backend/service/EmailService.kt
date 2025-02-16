package com.geochat.backend.service

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(private val mailSender: JavaMailSender) {

    fun sendResetCode(email: String, code: String) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message)

        helper.setTo(email)
        helper.setSubject("Восстановление пароля")
        helper.setText("Ваш код для сброса пароля: $code", false)

        mailSender.send(message)
    }
}
