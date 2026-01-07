package dev.protik.moneymanager.controller;

import dev.protik.moneymanager.service.DashboardService;
import dev.protik.moneymanager.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final EmailService emailService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboardData = dashboardService.getDashboardData();
        return ResponseEntity.ok(dashboardData);
    }

    // send mail test
    @GetMapping("/mail")
    public String sendMail(){
        emailService.sendMail(
                "protikdas018830@gmail.com",
                "Testing ",
                "Hope Working"
        );

        return "OK";
    }
}
