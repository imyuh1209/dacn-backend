package vn.bxh.jobhunter.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import vn.bxh.jobhunter.util.Constant.GenderEnum;
import vn.bxh.jobhunter.util.SecurityUtil;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Vui lòng nhập họ tên!")
    private String name;

    @Email(message = "Email không hợp lệ!")
    @NotBlank(message = "Vui lòng nhập email!")
    private String email;

    @NotBlank(message = "Vui lòng nhập mật khẩu!")
    @Size(min = 6, message = "Mật khẩu quá ngắn, tối thiểu 6 ký tự!")
    private String password;

    @NotNull(message = "Vui lòng nhập tuổi!")
    @Min(value = 16, message = "Tuổi tối thiểu là 16!")
    private Integer age;

    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    @NotBlank(message = "Vui lòng nhập địa chỉ!")
    private String address;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String refreshToken;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String forgotPasswordToken;
    private Instant forgotPasswordTokenExpiry;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    @OneToMany( mappedBy = "user")
    List<Resume> resumes;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        this.updatedAt = Instant.now();
    }
}
