package vn.bxh.jobhunter.domain.response;

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
public class ResJobDetailDTO {
    private Long id;
    private String name;
    private String description;
    private String location;
    private Double salary;
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

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkillDTO {
        private Long id;
        private String name;
    }
}