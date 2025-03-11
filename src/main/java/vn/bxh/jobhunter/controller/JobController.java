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
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.repository.JobRepository;
import vn.bxh.jobhunter.service.JobService;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.Optional;
import java.util.OptionalInt;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class JobController {
    private final JobService jobService;
    private final JobRepository jobRepository;
    @PostMapping("/jobs")
    public ResponseEntity<Job> createJob(@Valid @RequestBody Job job){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.jobService.HandleSaveJob(job));
    }

    @PutMapping("/jobs")
    public ResponseEntity<Job> updateJob(@RequestBody Job job){
        this.jobService.HandleUpdateJob(job);
        return ResponseEntity.ok(this.jobService.HandleSaveJob(job));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<Job> getJob(@PathVariable long id){
        return ResponseEntity.ok(this.jobService.FindJobById(id));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ResultPaginationDTO> getAllJobs(@Filter Specification<Job> spec, Pageable pageable){
        return ResponseEntity.ok(this.jobService.FindAllJobs(spec,pageable));
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable long id){
        this.jobService.DeleteById(id);
        return ResponseEntity.ok(null);
    }

}
