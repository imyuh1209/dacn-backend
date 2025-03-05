package vn.bxh.jobhunter.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Meta {
    private int page;
    private int pageSize;
    private int pages;
    private long total;
}
