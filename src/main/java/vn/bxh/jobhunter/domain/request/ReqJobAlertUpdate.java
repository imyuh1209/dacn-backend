package vn.bxh.jobhunter.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqJobAlertUpdate {
    @NotNull
    private Long id;

    private String keyword;
    private String location;
    private Double salaryMin;
    private Double salaryMax;
    private String level;
    private String companyName;

    private String frequency; // DAILY/WEEKLY
    private Boolean enabled;
}

