package vn.bxh.jobhunter.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqRegisterDTO {
    @NotBlank(message = "Vui lòng nhập họ tên!")
    private String name;

    @Email(message = "Email không hợp lệ!")
    @NotBlank(message = "Vui lòng nhập email!")
    private String email;

    @NotBlank(message = "Vui lòng nhập mật khẩu!")
    @Size(min = 6, message = "Mật khẩu quá ngắn, tối thiểu 6 ký tự!")
    private String password;
}
