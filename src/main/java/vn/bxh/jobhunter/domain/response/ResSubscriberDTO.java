package vn.bxh.jobhunter.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResSubscriberDTO {
    private long id;
    private String email;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
}