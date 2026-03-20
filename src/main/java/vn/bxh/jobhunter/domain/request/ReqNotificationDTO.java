package vn.bxh.jobhunter.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqNotificationDTO {
    @NotBlank(message = "Title cannot be empty")
    private String title;
    
    @NotBlank(message = "Message cannot be empty")
    private String message;
}
