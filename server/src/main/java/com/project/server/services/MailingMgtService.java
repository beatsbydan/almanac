package com.project.server.services;

import com.project.server.dtos.response.MessageResponse;
import com.project.server.exceptions.MailingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailingMgtService {
    private final JavaMailSender javaMailSender;

    public void sendOtp(String staffEmail, String otp) throws MailingException {
        String message = String.format("Hi there, please use this OTP: %s.", otp);
        sendMail(
                "OTP Verification",
                "betsbydan.dev@gmail.com",
                staffEmail,
                message

        );
    }
    private MessageResponse sendMail(
            String subject,
            String sender,
            String receiver,
            String message
    ) throws MailingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(receiver);
            mimeMessageHelper.setText(message);

            javaMailSender.send(mimeMessage);
        } catch(Exception ex){
            log.warn(ex.getMessage());
            throw new MailingException("Failed to send mail");
        }

        return new MessageResponse("Mail sent!");
    }
}
