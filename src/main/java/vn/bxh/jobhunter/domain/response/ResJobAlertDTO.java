package vn.bxh.jobhunter.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.bxh.jobhunter.domain.JobAlert;
import vn.bxh.jobhunter.util.Constant.JobAlertFrequencyEnum;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResJobAlertDTO {
    private Long id;
    private String criteria;
    private JobAlertFrequencyEnum frequency;
    private boolean enabled;
    private Instant lastSentAt;

    public static ResJobAlertDTO from(JobAlert a) {
        StringBuilder sb = new StringBuilder();
        if (a.getKeyword() != null && !a.getKeyword().isBlank()) sb.append(a.getKeyword()).append("; ");
        if (a.getLocation() != null && !a.getLocation().isBlank()) sb.append(a.getLocation()).append("; ");
        if (a.getCompanyName() != null && !a.getCompanyName().isBlank()) sb.append(a.getCompanyName()).append("; ");
        if (a.getLevel() != null && !a.getLevel().isBlank()) sb.append(a.getLevel()).append("; ");
        if (a.getSalaryMin() != null || a.getSalaryMax() != null) {
            String min = a.getSalaryMin() != null ? String.valueOf(a.getSalaryMin().longValue()) : "";
            String max = a.getSalaryMax() != null ? String.valueOf(a.getSalaryMax().longValue()) : "";
            String range = (min.isEmpty() && max.isEmpty()) ? "" : (min + "–" + max);
            if (!range.isEmpty()) sb.append(range).append("; ");
        }
        String c = sb.toString().trim();
        if (c.endsWith(";")) c = c.substring(0, c.length()-1);
        ResJobAlertDTO dto = new ResJobAlertDTO();
        dto.setId(a.getId());
        dto.setCriteria(c.isEmpty()? null : c);
        dto.setFrequency(a.getFrequency());
        dto.setEnabled(a.isEnabled());
        dto.setLastSentAt(a.getLastSentAt());
        return dto;
    }
}
