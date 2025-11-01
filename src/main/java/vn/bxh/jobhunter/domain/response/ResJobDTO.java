package vn.bxh.jobhunter.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import vn.bxh.jobhunter.util.Constant.LevelEnum;

@Getter
@Setter
public class ResJobDTO {
    private long id;
    private String name;
    private String location;
    private double salary;
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

    @Getter
    @Setter
    public static class ResCompanyDTO {
        private long id;
        private String name;
        private String logo; 
    }
}