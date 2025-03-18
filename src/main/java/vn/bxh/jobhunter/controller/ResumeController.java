package vn.bxh.jobhunter.controller;

import com.turkraft.springfilter.boot.Filter;
import com.turkraft.springfilter.builder.FilterBuilder;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.Company;
import vn.bxh.jobhunter.domain.Job;
import vn.bxh.jobhunter.domain.Resume;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.domain.response.Resume.ResResumeDTO;
import vn.bxh.jobhunter.repository.ResumeRepository;
import vn.bxh.jobhunter.repository.UserRepository;
import vn.bxh.jobhunter.service.ResumeService;
import vn.bxh.jobhunter.service.UserService;
import vn.bxh.jobhunter.util.SecurityUtil;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class ResumeController {
    private final ResumeRepository resumeRepository;
    private final ResumeService resumeService;
    private final UserRepository userRepository;
    private final FilterBuilder filterBuilder;
    private final FilterSpecificationConverter filterSpecificationConverter;

    @PostMapping("/resumes")
    public ResponseEntity<ResResumeDTO> createResume(@RequestBody Resume resume){
        if(this.resumeRepository.existsByEmail(resume.getEmail())){
            throw new IdInvalidException("Email is not valid !");
        }
        Resume resumeNew = this.resumeService.HandleCreateResume(resume);
        return ResponseEntity.status(HttpStatus.CREATED).
                body(this.resumeService.ConvertToResResumeDTO(resumeNew));
    }

    @PutMapping("/resumes")
    public ResponseEntity<Resume> UpdateResume(@RequestBody Resume resume){
        return ResponseEntity.ok(this.resumeService.HandleUpdateResume(resume));
    }

    @DeleteMapping("/resumes/{id}")
    public ResponseEntity<Void> DeleteResume(@PathVariable long id){
        this.resumeService.DeleteResume(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/resumes/{id}")
    public ResponseEntity<ResResumeDTO> GetResume(@PathVariable long id){
        return ResponseEntity.ok(this.resumeService.GetResume(id));
    }

    @GetMapping("/resumes")
    public ResponseEntity<ResultPaginationDTO> GetAllResume(@Filter Specification<Resume> spec, Pageable pageable){
        List<Long> arrJobIds = null;
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        User currentUser = this.userRepository.findByEmail(email);
        if (currentUser != null) {
            Company userCompany = currentUser.getCompany();
            if (userCompany != null) {
                List<Job> companyJobs = userCompany.getJobs();
                if (companyJobs != null && companyJobs.size() > 0) {
                    arrJobIds = companyJobs.stream().map(x -> x.getId())
                            .toList();
                }
            }
        }
        Specification<Resume> jobInSpec = filterSpecificationConverter.convert(filterBuilder.field("job")
                .in(filterBuilder.input(arrJobIds)).get());

        Specification<Resume> finalSpec = jobInSpec.and(spec);


        return ResponseEntity.ok(this.resumeService.GetAllResume(spec, pageable));
    }

    @PostMapping("/resumes/by-user")
    public ResponseEntity<ResultPaginationDTO> GetAllByUser(Pageable pageable){
        return ResponseEntity.ok(this.resumeService.GetAllByUser(pageable));
    }
}
