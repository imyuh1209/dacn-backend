package vn.bxh.jobhunter.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import vn.bxh.jobhunter.util.Constant.BannerPositionEnum;

import java.time.Instant;

@Getter
@Setter
public class ReqBannerUpsert {
    private Long id; // dùng cho PUT

    @NotBlank(message = "Title is required")
    private String title;

    private String image; // fileName đã upload

    // chỉ cho phép URL hợp lệ nếu có
    @Pattern(
            regexp = "^(https?://).*$",
            message = "Link must be a valid http(s) URL"
    )
    private String link;

    private BannerPositionEnum position = BannerPositionEnum.HOME; // vị trí hiển thị
    private boolean active = true;

    private Instant startDate;
    private Instant endDate;

    // Thứ tự hiển thị tuỳ chọn
    private Integer order;
}