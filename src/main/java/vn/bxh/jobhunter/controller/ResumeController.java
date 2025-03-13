package vn.bxh.jobhunter.controller;

import com.turkraft.springfilter.boot.Filter;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.Resume;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.domain.response.Resume.ResResumeDTO;
import vn.bxh.jobhunter.repository.ResumeRepository;
import vn.bxh.jobhunter.service.ResumeService;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class ResumeController {
    private final ResumeRepository resumeRepository;
    private final ResumeService resumeService;
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
        return ResponseEntity.ok(this.resumeService.GetAllResume(spec, pageable));
    }
}
