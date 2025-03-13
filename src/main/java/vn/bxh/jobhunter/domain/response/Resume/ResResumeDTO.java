package vn.bxh.jobhunter.domain.response.Resume;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import vn.bxh.jobhunter.util.Constant.ResumeStateEnum;

import java.time.Instant;

@Getter
@Setter
public class ResResumeDTO {
    private long id;
    private String email;
    private String url;
    private ResumeStateEnum status;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private User user;
    private Job job;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class User{
        private long id;
        private String name;
    }
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Job{
        private long id;
        private String name;
    }
}
