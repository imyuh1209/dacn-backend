package vn.bxh.jobhunter.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.Job;
import vn.bxh.jobhunter.domain.response.JobWithApplicantCountDTO;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.repository.JobRepository;
import vn.bxh.jobhunter.service.JobService;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class JobController {
    private final JobService jobService;
    private final JobRepository jobRepository;

    @GetMapping("/jobs-with-applicants")
    public ResponseEntity<List<JobWithApplicantCountDTO>> getJobsWithApplicantCount() {
        List<JobWithApplicantCountDTO> list = jobService.getAllJobsWithApplicantCountByCurrentUser();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/jobs")
    public ResponseEntity<Job> createJob(@Valid @RequestBody Job job){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.jobService.HandleSaveJob(job));
    }

    @PutMapping("/jobs")
    public ResponseEntity<Job> updateJob(@RequestBody Job job){
         return ResponseEntity.ok(this.jobRepository.save(this.jobService.HandleUpdateJob(job)));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<Job> getJob(@PathVariable long id){
        return ResponseEntity.ok(this.jobService.FindJobById(id));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ResultPaginationDTO> getAllJobs(@Filter Specification<Job> spec, Pageable pageable){
        return ResponseEntity.ok(this.jobService.FindAllJobs(spec,pageable));
    }

    @GetMapping("/jobs/by-company")
    public ResponseEntity<ResultPaginationDTO> getAllJobsByCurrentCompany(
            @Filter Specification<Job> spec, Pageable pageable) {

        Long companyId = this.jobService.getCurrentUserCompanyId();

        // Combine spec với điều kiện lọc theo companyId
        Specification<Job> companySpec = (root, query, cb) ->
                cb.equal(root.get("company").get("id"), companyId);

        Specification<Job> finalSpec = spec == null ? companySpec : spec.and(companySpec);

        return ResponseEntity.ok(this.jobService.FindAllJobs(finalSpec, pageable));
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable long id){
        this.jobService.DeleteById(id);
        return ResponseEntity.ok(null);
    }

}
