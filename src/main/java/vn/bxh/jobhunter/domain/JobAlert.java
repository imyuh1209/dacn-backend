package vn.bxh.jobhunter.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.bxh.jobhunter.util.Constant.JobAlertFrequencyEnum;

import java.time.Instant;

@Entity
@Table(name = "job_alerts", indexes = {
        @Index(name = "idx_job_alert_email", columnList = "email")
})
@Getter
@Setter
public class JobAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String keyword;
    private String location;
    private Double salaryMin;
    private Double salaryMax;
    private String level; // dùng chuỗi để linh hoạt với FE
    private String companyName;

    @Enumerated(EnumType.STRING)
    private JobAlertFrequencyEnum frequency; // DAILY/WEEKLY

    private boolean enabled;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastSentAt;
}

