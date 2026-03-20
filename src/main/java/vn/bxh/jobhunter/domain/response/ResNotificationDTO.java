package vn.bxh.jobhunter.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResNotificationDTO {
    private long id;
    private String title;
    private String message;
    private String senderName;
    private boolean isRead;
    private Instant createdAt;
}
