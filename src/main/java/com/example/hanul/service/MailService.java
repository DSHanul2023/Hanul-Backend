package com.example.hanul.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MailService {
    private final JavaMailSender javaMailSender;
    private static final String senderEmail = "hanul8939@gmail.com";

    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendTemporaryPasswordByEmail(String recipientEmail, String temporaryPassword) {
        MimeMessage message = createTemporaryPasswordEmail(recipientEmail, temporaryPassword);
        javaMailSender.send(message);
    }

    private MimeMessage createTemporaryPasswordEmail(String recipientEmail, String temporaryPassword) {
        // 이메일 내용을 생성하고 MimeMessage로 반환하는 코드를 작성하세요.
        // 이메일 제목, 내용 등을 설정하고 반환합니다.
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, recipientEmail);
            message.setSubject("임시 비밀번호 발급");
            String body = "요청하신 임시 비밀번호입니다: " + temporaryPassword;
            message.setText(body, "UTF-8", "html");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return message;
    }
}
