package vn.bxh.jobhunter.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqForgotPassword {
    @NotBlank(message = "Vui lòng nhập email!")
    @Email(message = "Email không hợp lệ!")
    private String email;
}
