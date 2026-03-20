package vn.bxh.jobhunter.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqRegisterDTO {
    @NotBlank(message = "Name cannot be empty!")
    private String name;

    @Email(message = "Invalid email format!")
    @NotBlank(message = "Email cannot be empty!")
    private String email;

    @NotBlank(message = "Password cannot be empty!")
    @Size(min = 3, message = "Password must be at least 3 characters!")
    private String password;
}
