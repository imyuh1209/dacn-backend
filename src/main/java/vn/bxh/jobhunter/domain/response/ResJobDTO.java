package vn.bxh.jobhunter.domain.response;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import vn.bxh.jobhunter.util.Constant.LevelEnum;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResJobDTO {
    private long id;
    private String name;
    private String location;
    private double salary;
    private Double salaryMin;
    private Double salaryMax;
    private int quantity;
    private LevelEnum level;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private List<String> skills;
    private ResCompanyDTO company;

    // Alias cho Flutter: lương từ/đến và cờ thỏa thuận
    @JsonProperty("salary_from")
    public Double getSalaryFrom() {
        if (salaryMin != null && salaryMin > 0) return salaryMin;
        return (salary > 0) ? Double.valueOf(salary) : null;
    }

    // CamelCase alias để hỗ trợ client khác
    @JsonProperty("salaryFrom")
    public Double getSalaryFromCamel() {
        return getSalaryFrom();
    }

    @JsonProperty("salary_to")
    public Double getSalaryTo() {
        if (salaryMax != null && salaryMax > 0) return salaryMax;
        return (salary > 0) ? Double.valueOf(salary) : null;
    }

    // CamelCase alias để hỗ trợ client khác
    @JsonProperty("salaryTo")
    public Double getSalaryToCamel() {
        return getSalaryTo();
    }

    @JsonProperty("is_negotiable")
    public boolean isNegotiable() {
        boolean hasMin = salaryMin != null && salaryMin > 0;
        boolean hasMax = salaryMax != null && salaryMax > 0;
        boolean hasFixed = salary > 0;
        return !hasMin && !hasMax && !hasFixed;
    }

    // CamelCase alias để hỗ trợ client khác
    @JsonProperty("isNegotiable")
    public boolean isNegotiableCamel() {
        return isNegotiable();
    }

    @Getter
    @Setter
    public static class ResCompanyDTO {
        private long id;
        private String name;
        private String logo; 
        public String getLogoUrl() {
            if (logo == null || logo.isEmpty()) return null;
            try {
                return "/storage/company/" + java.net.URLEncoder.encode(logo, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                return "/storage/company/" + logo;
            }
        }
    }
}