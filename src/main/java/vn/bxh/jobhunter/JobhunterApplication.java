package vn.bxh.jobhunter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
// @SpringBootApplication(exclude =
// org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
public class JobhunterApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(JobhunterApplication.class, args);
		// Lấy MailSender sau khi Spring đã khởi động
		System.out.println(context.getBean(MailSender.class));
	}

}
