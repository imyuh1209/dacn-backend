package vn.bxh.jobhunter.domain.response;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.bxh.jobhunter.util.Constant.GenderEnum;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResUserDTO {
    private long id;
    private String email;
    private String name;
    private GenderEnum gender;
    private String address;
    private int age;
    private Instant updatedAt;
    private Instant createdAt;
    private ResCompanyDTO Company;
    private UserRole role;
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRole{
        private long id;
        private String name;
    }
}
