package vn.bxh.jobhunter.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.bxh.jobhunter.util.Constant.LevelEnum;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResJobDetailDTO {
    private Long id;
    private String name;
    private String description;
    private String location;
    private Double salary;
    private Double salaryMin;
    private Double salaryMax;
    private Integer quantity;
    private LevelEnum level;
    private Instant startDate;
    private Instant endDate;
    private boolean active;

    private ResCompanyDTO company;
    private List<SkillDTO> skills;

    private boolean saved;   // người dùng hiện tại đã lưu job này
    private boolean applied; // người dùng hiện tại đã ứng tuyển job này
    private Long applicantCount; // số lượng hồ sơ ứng tuyển

    // Alias cho Flutter: lương từ/đến và cờ thỏa thuận
    @JsonProperty("salary_from")
    public Double getSalaryFrom() {
        return (salaryMin != null) ? salaryMin : salary;
    }

    // CamelCase alias để hỗ trợ client khác
    @JsonProperty("salaryFrom")
    public Double getSalaryFromCamel() {
        return getSalaryFrom();
    }

    @JsonProperty("salary_to")
    public Double getSalaryTo() {
        return (salaryMax != null) ? salaryMax : salary;
    }

    // CamelCase alias để hỗ trợ client khác
    @JsonProperty("salaryTo")
    public Double getSalaryToCamel() {
        return getSalaryTo();
    }

    @JsonProperty("is_negotiable")
    public boolean isNegotiable() {
        return salary == null && salaryMin == null && salaryMax == null;
    }

    // CamelCase alias để hỗ trợ client khác
    @JsonProperty("isNegotiable")
    public boolean isNegotiableCamel() {
        return isNegotiable();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkillDTO {
        private Long id;
        private String name;
    }
}