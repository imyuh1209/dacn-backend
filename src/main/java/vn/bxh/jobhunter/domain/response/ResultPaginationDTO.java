package vn.bxh.jobhunter.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultPaginationDTO {
    private Meta meta;
    private Object result;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Meta {
        private int page;
        private int pageSize;
        private int pages;
        private long total;
    }
}

