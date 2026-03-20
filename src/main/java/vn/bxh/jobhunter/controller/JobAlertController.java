package vn.bxh.jobhunter.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.JobAlert;
import vn.bxh.jobhunter.domain.request.ReqJobAlertUpsert;
import vn.bxh.jobhunter.domain.request.ReqJobAlertUpdate;
import vn.bxh.jobhunter.domain.response.ResJobAlertDTO;
import vn.bxh.jobhunter.service.JobAlertService;
import vn.bxh.jobhunter.util.anotation.ApiMessage;
import vn.bxh.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JobAlertController {

    private final JobAlertService jobAlertService;

    @PostMapping("/job-alerts")
    @ApiMessage("Tạo Job Alert thành công")
    public ResponseEntity<JobAlert> create(@Valid @RequestBody ReqJobAlertUpsert req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new IdInvalidException("Email không được để trống");
        }
        JobAlert alert = jobAlertService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(alert);
    }

    @GetMapping("/job-alerts")
    @ApiMessage("Danh sách Job Alerts theo email hoặc người hiện tại")
    public ResponseEntity<java.util.List<ResJobAlertDTO>> list(@RequestParam(name = "email", required = false) String email) {
        java.util.List<JobAlert> alerts = jobAlertService.listByEmailOrCurrent(email);
        java.util.List<ResJobAlertDTO> res = alerts.stream().map(ResJobAlertDTO::from).toList();
        return ResponseEntity.ok(res);
    }

    @PutMapping("/job-alerts")
    @ApiMessage("Cập nhật Job Alert của chủ sở hữu")
    public ResponseEntity<ResJobAlertDTO> update(@RequestBody ReqJobAlertUpdate req) {
        String email = vn.bxh.jobhunter.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Không xác định được người dùng hiện tại"));
        JobAlert alert = jobAlertService.updateOwned(email, req);
        return ResponseEntity.ok(ResJobAlertDTO.from(alert));
    }

    @DeleteMapping("/job-alerts/{id}")
    @ApiMessage("Xóa Job Alert của chủ sở hữu")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        String email = vn.bxh.jobhunter.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Không xác định được người dùng hiện tại"));
        jobAlertService.deleteOwned(email, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/job-alerts/{id}/run")
    @ApiMessage("Thực thi gửi Job Alert ngay")
    public ResponseEntity<Void> runNow(@PathVariable Long id) {
        String email = vn.bxh.jobhunter.util.SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Không xác định được người dùng hiện tại"));
        jobAlertService.runNowOwned(email, id);
        return ResponseEntity.ok().build();
    }
}
