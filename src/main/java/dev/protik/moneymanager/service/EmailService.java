package dev.protik.moneymanager.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    private String fromEmail = "protikdas018830@gmail.com";
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void sendMail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // ✅ MUST BE TRUE
            helper.setFrom(fromEmail, "Protik the DEV");

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Email sending failed", e);
        }
    }
}
