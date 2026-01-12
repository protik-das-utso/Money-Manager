package dev.protik.moneymanager.service;

import dev.protik.moneymanager.entity.ProfileEntity;
import dev.protik.moneymanager.repository.ProfileRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ProfileService profileService;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final ProfileRepository profileRepository;
    private final JavaMailSender mailSender;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Dhaka")
    public void sendDailyIncomeExpenseReminder() {
        log.info("Job Statred : sendDailyIncomeExpenseReminder()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile : profiles) {
            String body =
                    "<div style='background:#f4f6f8; padding:5px; font-family:Arial, Helvetica, sans-serif;'>" +

                            "<div style='max-width:600px; margin:0 auto; background:#ffffff; padding:25px; " +
                            "border-radius:8px; box-shadow:0 4px 10px rgba(0,0,0,0.08);'>" +

                            "<h2 style='color:#2c3e50; margin-top:0;'>Hello " + profile.getFullName() + " 👋</h2>" +

                            "<p style='color:#555; font-size:15px; line-height:1.6;'>" +
                            "This is a friendly reminder to add or manage your income and expenses." +
                            "</p>" +

                            "<p style='color:#555; font-size:15px;'>Keeping your records up to date helps you:</p>" +

                            "<ul style='color:#555; font-size:14px; line-height:1.8; padding-left:18px;'>" +
                            "<li>Track where your money is going 💰</li>" +
                            "<li>Stay in control of your budget 📊</li>" +
                            "<li>Make better financial decisions 🚀</li>" +
                            "</ul>" +

                            "<div style='text-align:center; margin:30px 0;'>" +
                            "<a href='" + frontendUrl + "' " +
                            "style='background:#3498db; color:#ffffff; text-decoration:none; " +
                            "padding:12px 25px; border-radius:6px; font-size:15px; display:inline-block;'>" +
                            "Manage Income & Expenses" +
                            "</a>" +
                            "</div>" +

                            "<p style='color:#777; font-size:13px;'>" +
                            "It only takes a minute to stay organized." +
                            "</p>" +

                            "<hr style='border:none; border-top:1px solid #eaeaea; margin:25px 0;'>" +

                            "<p style='color:#999; font-size:12px; text-align:center;'>" +
                            "<strong>Protik The DEV</strong><br>" +
                            "© 2026 Money Manager" +
                            "</p>" +

                            "</div>" +
                            "</div>";
            sendMail(profile.getEmail(), "Daily Remider: Add your Income & Expense", body);

        }

    }
    public void sendMail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // ✅ THIS IS THE KEY
//            helper.setFrom("noreply@moneymanager.com");

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    // per day not

    // per week summary

    // per month summary

    // when expense is more than imcome in a week

}
