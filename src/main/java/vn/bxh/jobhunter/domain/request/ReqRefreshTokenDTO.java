package vn.bxh.jobhunter.domain.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqRefreshTokenDTO {
    @NotBlank(message = "refreshToken is required")
    @JsonAlias({"refresh_token", "refreshToken"})
    private String refreshToken;
}