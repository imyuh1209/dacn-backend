package vn.bxh.jobhunter.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import vn.bxh.jobhunter.domain.Job;

@Getter
@Setter
@AllArgsConstructor
public class JobSimpleDTO {
    private Long id;
    private String name;

    public static JobSimpleDTO from(Job j) {
        return new JobSimpleDTO(j.getId(), j.getName());
    }
}