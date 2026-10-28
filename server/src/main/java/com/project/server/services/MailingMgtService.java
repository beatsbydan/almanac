package com.project.server.services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Mail is sent off the request thread (@Async, see AsyncConfig) — a request that triggers an OTP
 * no longer waits on an SMTP round-trip to get a response. The tradeoff, accepted deliberately
 * rather than left as a silent gap: if sending genuinely fails, the caller has already gotten a
 * success response, so the only visibility into that failure is the ERROR log line below, not a
 * synchronous error back to the user. A real retry/outbox pattern (Blueprint §06) is the fuller
 * fix; this is the contained version of "stop blocking the request thread on mail."
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MailingMgtService {
    private final JavaMailSender javaMailSender;

    @Async
    public void sendOtp(String staffEmail, String otp) {
        String message = String.format("Hi there, please use this OTP: %s.", otp);
        sendMail(
                "OTP Verification",
                "betsbydan.dev@gmail.com",
                staffEmail,
                message
        );
    }

    private void sendMail(
            String subject,
            String sender,
            String receiver,
            String message
    ) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(receiver);
            mimeMessageHelper.setText(message);

            javaMailSender.send(mimeMessage);
        } catch(Exception ex){
            log.error("Failed to send mail to {}: {}", receiver, ex.getMessage());
        }
    }
}
