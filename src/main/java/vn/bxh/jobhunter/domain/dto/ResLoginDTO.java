package vn.bxh.jobhunter.domain.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResLoginDTO {
    private String accessToken;
    private UserLogin user;

    @Getter
    @Setter
    public static class UserLogin{
        private long id;
        private String email;
        private String name;
    }
}
