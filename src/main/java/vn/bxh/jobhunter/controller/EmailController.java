package vn.bxh.jobhunter.controller;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.bxh.jobhunter.service.EmailService;

@RestController
@RequestMapping("/api/v1")
public class EmailController {
    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

//    @Scheduled(cron = "*/10 * * * * *")
//    @Transactional
    @GetMapping("/email")
    public String SendEmail(){
        this.emailService.sendSubscribersEmailJobs();
        return "oke";
    }

}
