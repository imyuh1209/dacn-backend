package vn.bxh.jobhunter.controller;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.bxh.jobhunter.service.EmailService;
import vn.bxh.jobhunter.service.SubscriberService;

@RestController
@RequestMapping("/api/v1")
public class EmailController {
    private final EmailService emailService;
    private final SubscriberService subscriberService;

    public EmailController(EmailService emailService,SubscriberService subscriberService) {
        this.emailService = emailService;
        this.subscriberService = subscriberService;
    }

    @Scheduled(cron = "0 0 19 ? * SAT")
    @Transactional
    @GetMapping("/email")
    public String SendEmail(){
        this.subscriberService.sendSubscribersEmailJobs();
        return "oke";
    }

}
