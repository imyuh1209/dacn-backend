package vn.bxh.jobhunter.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqJobAlertUpsert {
    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    private String keyword;
    private String location;
    private Double salaryMin;
    private Double salaryMax;
    private String level;
    private String companyName;

    @NotBlank(message = "Tần suất không được để trống")
    private String frequency; // DAILY/WEEKLY

    @NotNull(message = "Trạng thái bật/tắt không được để trống")
    private Boolean enabled;
}

