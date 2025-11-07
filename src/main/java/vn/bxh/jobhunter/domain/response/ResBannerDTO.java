package vn.bxh.jobhunter.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResBannerDTO {
    private Long id;
    private String title;
    private String image; // fileName
    private String link;
    private boolean active;
    private Instant startDate;
    private Instant endDate;
    private Integer order; // thứ tự hiển thị
    private String position; // vị trí hiển thị: HOME
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    public String getImageUrl() {
        if (image == null || image.isEmpty()) return null;
        try {
            return "/storage/banner/" + java.net.URLEncoder.encode(image, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "/storage/banner/" + image;
        }
    }
}