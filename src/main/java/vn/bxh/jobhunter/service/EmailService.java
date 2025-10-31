package vn.bxh.jobhunter.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Synchronized;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.bxh.jobhunter.domain.*;
import vn.bxh.jobhunter.domain.response.Email.ResEmailJob;
import vn.bxh.jobhunter.repository.JobRepository;
import vn.bxh.jobhunter.repository.SubscriberRepository;
import vn.bxh.jobhunter.repository.UserRepository;
import vn.bxh.jobhunter.util.SecurityUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmailService {
    private final MailSender mailSender;
    private final TemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final SubscriberRepository subscriberRepository;
    private final JobRepository jobRepository;

    private static final String FROM_EMAIL = "JobHunter@gmail.com";


    private final String[] subjects = {
            "Cậu rảnh không? Tớ có chuyện muốn hỏi",
            "Nhớ cậu rồi, lâu quá không gặp!",
            "Có gì mới không? Chia sẻ chút đi!",
            "Tớ vừa thấy cái này và nhớ đến cậu",
            "Chỉ là một email bình thường thôi "
    };

    private final String[] messages = {
            "Hôm nay tớ vừa đi ngang qua quán cà phê mà tụi mình hay ngồi. Nhớ lại mấy lần tám chuyện vui ghê! Khi nào rảnh làm một kèo nhé?",
            "Dạo này thế nào rồi? Có gì vui không? Tớ đang muốn tìm vài bộ phim hay để xem, cậu có gợi ý nào không?",
            "Không có gì đặc biệt đâu, chỉ là tự nhiên nhớ đến cậu nên gửi email này thôi. Hy vọng cậu có một ngày thật tuyệt vời!",
            "Tớ vừa đọc một bài viết khá hay và thấy khá giống tình huống của cậu trước đây. Khi nào có thời gian, gửi tớ một email nhé!",
            "Hôm nay hơi rảnh nên gửi email cho vài người bạn. Nếu cậu thấy email này, chắc chắn cậu là người đặc biệt đó!"
    };

//    @Scheduled(cron = "*/10 * * * * *") // Gửi mỗi 5 phút
//    @Transactional
    public void sendRandomEmail() {
        Random random = new Random();
        String subject = subjects[random.nextInt(subjects.length)];
        String text = messages[random.nextInt(messages.length)];

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(FROM_EMAIL);
        msg.setTo("trinhquangkhai2010@gmail.com"); // Thay đổi email người nhận nếu cần
        msg.setSubject(subject);
        msg.setText(text);

        mailSender.send(msg);
        System.out.println("Email đã gửi: " + subject);
    }

    public String sendConfirmationEmail() {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(FROM_EMAIL);
        msg.setTo("haogolike1@gmail.com");
        msg.setSubject("Testing from Spring Boot");
        msg.setText("Hello World from Spring Boot Email");
        mailSender.send(msg);
        return "OK";
    }

    public void sendEmailSync(String to, String subject, String content, boolean isMultipart,
                              boolean isHtml) {
        // Prepare message using a Spring helper
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage,
                    isMultipart, StandardCharsets.UTF_8.name());
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content, isHtml);
            this.javaMailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            System.out.println("ERROR SEND EMAIL: " + e);
        }
    }
    @Async
    public void sendEmailFromTemplateSync(
            String to,
            String subject,
            String templateName,
            String username,
            Object value) {

        Context context = new Context();
        context.setVariable("name", username);
        context.setVariable("jobs", value);

        String content = templateEngine.process(templateName, context);
        this.sendEmailSync(to, subject, content, false, true);
    }

    public void sendResumeStatusEmail(String to,
                                      String subject,
                                      String candidateName,
                                      String positionName,
                                      String companyName,
                                      String status) {
        Context context = new Context();
        context.setVariable("candidateName", candidateName);
        context.setVariable("positionName", positionName);
        context.setVariable("companyName", companyName);

        String templateName;
        String st = status == null ? "" : status.trim().toUpperCase();
        switch (st) {
            case "PENDING":
                templateName = "resume-status-pending";
                break;
            case "REVIEWING":
                templateName = "resume-status-reviewing";
                break;
            case "APPROVED":
                templateName = "resume-status-approved";
                break;
            case "REJECTED":
                templateName = "resume-status-rejected";
                break;
            default:
                templateName = "resume-status"; // fallback đơn giản
        }

        String content = templateEngine.process(templateName, context);
        this.sendEmailSync(to, subject, content, false, true);
    }


}
