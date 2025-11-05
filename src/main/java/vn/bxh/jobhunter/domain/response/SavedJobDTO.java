package vn.bxh.jobhunter.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavedJobDTO {
    private Long id;       // id của savedJob
    private Long jobId;
    private String jobName;
    private String companyName;
    private String companyLogo; 
    public String getCompanyLogoUrl() {
        if (companyLogo == null || companyLogo.isEmpty()) return null;
        try {
            return "/storage/company/" + java.net.URLEncoder.encode(companyLogo, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "/storage/company/" + companyLogo;
        }
    }
    private String location;
    private Double salary;
    private Double salaryMin;
    private Double salaryMax;
    private String level;

    // Alias cho Flutter: lương từ/đến và cờ thỏa thuận
    @JsonProperty("salary_from")
    public Double getSalaryFrom() {
        return (salaryMin != null && salaryMin > 0) ? salaryMin : salary;
    }

    @JsonProperty("salary_to")
    public Double getSalaryTo() {
        return (salaryMax != null && salaryMax > 0) ? salaryMax : salary;
    }

    @JsonProperty("is_negotiable")
    public boolean isNegotiable() {
        boolean hasMin = salaryMin != null && salaryMin > 0;
        boolean hasMax = salaryMax != null && salaryMax > 0;
        boolean hasFixed = salary != null && salary > 0;
        return !hasMin && !hasMax && !hasFixed;
    }

    // CamelCase alias song song
    @JsonProperty("salaryFrom")
    public Double getSalaryFromCamel() { return getSalaryFrom(); }

    @JsonProperty("salaryTo")
    public Double getSalaryToCamel() { return getSalaryTo(); }

    @JsonProperty("isNegotiable")
    public boolean isNegotiableCamel() { return isNegotiable(); }

    // Alias bổ sung cho tên job và tên công ty để app đọc linh hoạt
    @JsonProperty("name")
    public String getNameAlias() { return jobName; }

    @JsonProperty("title")
    public String getTitleAlias() { return jobName; }

    @JsonProperty("company_name")
    public String getCompanyNameSnake() { return companyName; }

    @JsonProperty("companyLogoURL")
    public String getCompanyLogoURLPascal() { return getCompanyLogoUrl(); }

    @JsonProperty("company_logo_url")
    public String getCompanyLogoUrlSnake() { return getCompanyLogoUrl(); }
}
