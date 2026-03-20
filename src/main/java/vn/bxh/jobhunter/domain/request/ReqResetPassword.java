package vn.bxh.jobhunter.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqResetPassword {
    @NotBlank(message = "Token cannot be empty!")
    private String token;

    @NotBlank(message = "New password cannot be empty!")
    @Size(min = 3, message = "Password must be at least 3 characters!")
    private String newPassword;
}