package vn.bxh.jobhunter.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResCompanyDetailDTO {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String logo; 

    private List<JobSimpleDTO> jobs; // danh sách job đơn giản của công ty
}