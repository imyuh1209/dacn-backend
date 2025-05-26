package vn.bxh.jobhunter.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobWithApplicantCountDTO {
    private Long id;
    private String name;
    private String location;
    private double salary;
    private int quantity;
    private boolean active;
    private Long applicantCount;
}
