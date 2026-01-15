package dev.protik.moneymanager.service;

import dev.protik.moneymanager.dto.ExpenseDTO;
import dev.protik.moneymanager.entity.ProfileEntity;
import dev.protik.moneymanager.repository.ExpenseRepository;
import dev.protik.moneymanager.repository.ProfileRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ProfileService profileService;
    private final EmailService emailService;
    private final ExpenseService expenseService;
    private final ExpenseRepository expenseRepository;

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final ProfileRepository profileRepository;
    private final JavaMailSender mailSender;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 21 * * *", zone = "Asia/Dhaka")
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
        log.info("Job Finished : sendDailyIncomeExpenseReminder()");

    }

    // send daily expense summary
    @Scheduled(cron = "0 0 20 * * *", zone = "Asia/Dhaka")
    public void sendDailyExpenseSummary() {
        log.info("Job Statred : sendDailyExpenseSummary()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile : profiles) {
            List<ExpenseDTO> todayExpenses = expenseService.getTodayExpensesByProfileId(profile.getEmail());
            if (!todayExpenses.isEmpty()) {
                StringBuilder table = new StringBuilder();

                // Calculate total
                BigDecimal totalAmount = BigDecimal.ZERO;

                for (ExpenseDTO expense : todayExpenses) {
                    totalAmount = totalAmount.add(expense.getAmount());
                }


                // Table start
                table.append("<table style='width:100%; border-collapse:collapse; font-family:Arial, sans-serif;'>");

                // Table header
                table.append("<tr style='background:#2f80ed; color:#ffffff;'>")
                        .append("<th style='padding:10px; border:1px solid #ddd;'>#</th>")
                        .append("<th style='padding:10px; border:1px solid #ddd;'>Category</th>")
                        .append("<th style='padding:10px; border:1px solid #ddd;'>Amount</th>")
                        .append("<th style='padding:10px; border:1px solid #ddd;'>Note</th>")
                        .append("</tr>");

                int i = 1;
                boolean alternate = false;

                for (ExpenseDTO expense : todayExpenses) {
                    String bgColor = alternate ? "#f9fafb" : "#ffffff";
                    alternate = !alternate;

                    table.append("<tr style='background:").append(bgColor).append(";'>")
                            .append("<td style='padding:8px; border:1px solid #ddd; text-align:center;'>").append(i++).append("</td>")
                            .append("<td style='padding:8px; border:1px solid #ddd;'>").append(expense.getCategoryId() != null ? expense.getCategoryName() : "N/A").append("</td>")
                            .append("<td style='padding:8px; border:1px solid #ddd; text-align:right;'>৳ ")
                            .append(expense.getAmount()).append("</td>")
                            .append("<td style='padding:8px; border:1px solid #ddd;'>")
                            .append(expense.getNote() != null ? expense.getNote() : "N/A")
                            .append("</td>")
                            .append("</tr>");
                }

                // Total row
                table.append("<tr style='background:#f1f3f5; font-weight:bold;'>")
                        .append("<td colspan='2' style='padding:10px; border:1px solid #ddd; text-align:right;'>Total</td>")
                        .append("<td style='padding:10px; border:1px solid #ddd; text-align:right;'>৳ ")
                        .append(totalAmount)
                        .append("</td>")
                        .append("<td style='border:1px solid #ddd;'></td>")
                        .append("</tr>");

                table.append("</table>");

                // Email body
                String body =
                        "<div style='max-width:600px; margin:auto; background:#ffffff; padding:20px; border-radius:8px; " +
                                "box-shadow:0 2px 8px rgba(0,0,0,0.05); font-family:Arial, sans-serif;'>" +

                                "<h2 style='color:#2f80ed; margin-top:0;'>Daily Expense Summary</h2>" +

                                "<p style='font-size:14px; color:#333;'>Hi <strong>" + profile.getFullName() + "</strong>,</p>" +

                                "<p style='font-size:14px; color:#555;'>Here is the summary of your expenses for today:</p>" +

                                table +

                                "<p style='margin-top:20px; font-size:13px; color:#666;'>" +
                                "Track your spending daily to stay financially healthy 💙" +
                                "</p>" +

                                "<hr style='border:none; border-top:1px solid #eaeaea; margin:25px 0;'>" +

                                "<p style='color:#999; font-size:12px; text-align:center;'>" +
                                "<strong>Protik The DEV</strong><br>" +
                                "Money Manager © 2026<br>" +
                                "All rights reserved" +
                                "</p>" +

                                "</div>";

                emailService.sendMail(
                        profile.getEmail(),
                        "📊 Your Daily Expense Summary",
                        body
                );

            }

        }
        log.info("Job Finished : sendDailyExpenseSummary()");
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

    // when expense is more than income in a week

}
