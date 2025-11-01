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
import vn.bxh.jobhunter.domain.Skill;
import vn.bxh.jobhunter.domain.response.JobWithApplicantCountDTO;
import vn.bxh.jobhunter.domain.response.ResCompanyDTO;
import vn.bxh.jobhunter.domain.response.ResJobDetailDTO;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.repository.JobRepository;
import vn.bxh.jobhunter.service.JobService;
import vn.bxh.jobhunter.service.ResumeService;
import vn.bxh.jobhunter.service.SavedJobService;
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
    private final ResumeService resumeService;
    private final SavedJobService savedJobService;

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
    public ResponseEntity<ResJobDetailDTO> getJob(@PathVariable long id){
        Job job = this.jobService.FindJobById(id);

        ResJobDetailDTO dto = new ResJobDetailDTO();
        dto.setId(job.getId());
        dto.setName(job.getName());
        dto.setDescription(job.getDescription());
        dto.setLocation(job.getLocation());
        dto.setSalary(job.getSalary());
        dto.setQuantity(job.getQuantity());
        dto.setLevel(job.getLevel());
        dto.setStartDate(job.getStartDate());
        dto.setEndDate(job.getEndDate());
        dto.setActive(job.isActive());

        if (job.getCompany() != null) {
            ResCompanyDTO companyDTO = new ResCompanyDTO();
            companyDTO.setId(job.getCompany().getId());
            companyDTO.setName(job.getCompany().getName());
            companyDTO.setDescription(job.getCompany().getDescription());
            companyDTO.setAddress(job.getCompany().getAddress());
            companyDTO.setLogo(job.getCompany().getLogo());
            dto.setCompany(companyDTO);
        }

        java.util.List<ResJobDetailDTO.SkillDTO> skillDTOs = new java.util.ArrayList<>();
        if (job.getSkills() != null) {
            for (Skill s : job.getSkills()) {
                skillDTOs.add(new ResJobDetailDTO.SkillDTO(s.getId(), s.getName()));
            }
        }
        dto.setSkills(skillDTOs);

        boolean saved = savedJobService.isSaved(job.getId());
        boolean applied = resumeService.isApplied(job.getId());
        long applicantCount = resumeService.countResumesByJob(job.getId());
        dto.setSaved(saved);
        dto.setApplied(applied);
        dto.setApplicantCount(applicantCount);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/jobs")
    public ResponseEntity<ResultPaginationDTO> getAllJobs(@Filter Specification<Job> spec, Pageable pageable){
        return ResponseEntity.ok(this.jobService.FindAllJobs(spec,pageable));
    }

    @GetMapping("/jobs/by-company")
    public ResponseEntity<List<vn.bxh.jobhunter.domain.response.JobSimpleDTO>> getJobsByCurrentCompany() {
        List<vn.bxh.jobhunter.domain.response.JobSimpleDTO> jobs = jobService.getJobsByCurrentCompanySimple();
        return ResponseEntity.ok(jobs);
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable long id){
        this.jobService.DeleteById(id);
        return ResponseEntity.ok(null);
    }

}
