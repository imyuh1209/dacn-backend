package vn.bxh.jobhunter.domain.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqResumeStatusEmail {
    @NotNull(message = "resumeId là bắt buộc")
    @Min(value = 1, message = "resumeId phải >= 1")
    private Long resumeId;

    @NotBlank(message = "status là bắt buộc")
    private String status;
}