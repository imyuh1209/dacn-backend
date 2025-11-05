package vn.bxh.jobhunter.domain.request;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import vn.bxh.jobhunter.util.Constant.GenderEnum;

@Setter
@Getter
public class ReqUserUpdate {

    private long id;

    @NotBlank(message = "Name cannot be empty!")
    private String name;

    @NotNull(message = "Age cannot be null!")
    @Min(value = 16, message = "Age must be at least 16!")
    private Integer age;

    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    @NotBlank(message = "Address cannot be empty!")
    private String address;

    // Optional: hỗ trợ cập nhật role theo cả hai dạng roleId hoặc role:{id}
    private Long roleId;
    private vn.bxh.jobhunter.domain.Role role;

}
