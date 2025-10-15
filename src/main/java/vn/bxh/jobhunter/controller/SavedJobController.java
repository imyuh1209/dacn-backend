package vn.bxh.jobhunter.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.SavedJob;
import vn.bxh.jobhunter.domain.response.SavedJobDTO;
import vn.bxh.jobhunter.service.SavedJobService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SavedJobController {

    private final SavedJobService service;

    // Lưu job
    @PostMapping("/saved-jobs")
    public ResponseEntity<SavedJobDTO> saveJob(@RequestParam("jobId") Long jobId) {
        SavedJob sj = service.saveJob(jobId);
        SavedJobDTO dto = toDTO(sj);
        return ResponseEntity.ok(dto);
    }

    // Danh sách job đã lưu của tôi
    @GetMapping("/saved-jobs")
    public ResponseEntity<List<SavedJobDTO>> listMySavedJobs() {
        List<SavedJobDTO> result = service.listMySavedJobs()
                .stream().map(this::toDTO).toList();
        return ResponseEntity.ok(result);
    }

    // Bỏ lưu theo savedId (hoặc bạn có thể dùng byJobId=true để xoá theo jobId)
    @DeleteMapping("/saved-jobs/{id}")
    public ResponseEntity<Void> removeSaved(@PathVariable Long id,
                                            @RequestParam(value = "byJobId", defaultValue = "false") boolean byJobId) {
        service.removeSavedJob(id, byJobId);
        return ResponseEntity.noContent().build();
    }


    private SavedJobDTO toDTO(SavedJob sj) {
        return new SavedJobDTO(
                sj.getId(),
                sj.getJob().getId(),
                sj.getJob().getName(),
                sj.getJob().getCompany() != null ? sj.getJob().getCompany().getName() : null,
                sj.getJob().getCompany() != null ? sj.getJob().getCompany().getLogo() : null,
                sj.getJob().getLocation(),
                sj.getJob().getSalary(),
                sj.getJob().getLevel().name()
        );
    }
}