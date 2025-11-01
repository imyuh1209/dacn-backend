package vn.bxh.jobhunter.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class SavedJobDTO {
    private Long id;       // id của savedJob
    private Long jobId;
    private String jobName;
    private String companyName;
    private String companyLogo; 
    private String location;
    private Double salary;
    private String level;
}
