package vn.bxh.jobhunter.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.bxh.jobhunter.util.SecurityUtil;
import vn.bxh.jobhunter.util.Constant.BannerPositionEnum;

import java.time.Instant;

@Entity
@Table(name = "banners")
@Getter
@Setter
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String image; // fileName đã upload
    private String link; // URL đích khi click
    private boolean active;
    private Instant startDate;
    private Instant endDate;
    private Integer position; // thứ tự hiển thị

    @Enumerated(EnumType.STRING)
    private BannerPositionEnum placement; // vị trí hiển thị: HOME

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.createdAt = Instant.now();
        if (this.active) {
            // đảm bảo position khác null khi active
            if (this.position == null) this.position = 0;
        }
        if (this.placement == null) {
            this.placement = BannerPositionEnum.HOME;
        }
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.updatedAt = Instant.now();
    }
}