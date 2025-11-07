package vn.bxh.jobhunter.domain.response.Resume;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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

    public String getUrlStorage() {
        if (url == null || url.isEmpty()) return null;
        try {
            return "/storage/resume/" + java.net.URLEncoder.encode(url, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "/storage/resume/" + url;
        }
    }

    // --- Convenience fields for FE web & Flutter ---
    // originalName: phần tên gốc khi upload (sau tiền tố timestamp-)
    public String getOriginalName() {
        if (url == null || url.isEmpty()) return null;
        int idx = url.indexOf("-");
        if (idx > -1 && idx + 1 < url.length()) return url.substring(idx + 1);
        return url;
    }

    // fileName: tên file lưu trong storage (đúng với url)
    public String getFileName() {
        return url;
    }

    // uploadedAt: map từ createdAt
    public Instant getUploadedAt() {
        return createdAt;
    }

    // type: suy từ phần mở rộng file
    public String getType() {
        String name = getOriginalName();
        if (name == null || name.isBlank()) return null;
        String lower = name.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User{
        private long id;
        private String name;
    }
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Job{
        private long id;
        private String name;
        private String companyName;
    }
}
