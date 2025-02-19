package com.geochat.backend.service

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(private val mailSender: JavaMailSender) {

    fun sendVerificationCode(email: String, code: String) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)

        helper.setTo(email)
        helper.setSubject("Восстановление пароля - GeoChat")
        helper.setText("Ваш код для восстановления пароля: <b>$code</b>", true)

        mailSender.send(message)
    }
}
