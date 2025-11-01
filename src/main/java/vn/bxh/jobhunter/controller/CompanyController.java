package vn.bxh.jobhunter.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.Company;
import vn.bxh.jobhunter.domain.Job;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.domain.response.ResCompanyDetailDTO;
import vn.bxh.jobhunter.domain.response.JobSimpleDTO;
import vn.bxh.jobhunter.repository.CompanyRepository;
import vn.bxh.jobhunter.service.CompanyService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class CompanyController {
    private final CompanyService companyService;
    private final CompanyRepository companyRepository;

    @PostMapping("/companies")
    public ResponseEntity<Company> createNewUser(@Valid @RequestBody Company company) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.companyService.HandleSaveCompany(company));
    }

    @GetMapping("/companies")
    public ResponseEntity<ResultPaginationDTO> getListCompanies(@Filter Specification<Company> spec, Pageable pageable){

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(this.companyService.FetchAllCompanies(spec, pageable));
    }

    @GetMapping("/companies/{id}")
    public ResponseEntity<ResCompanyDetailDTO> getCompany(@PathVariable long id){
        Company company = this.companyService.FetchCompany(id);
        ResCompanyDetailDTO dto = new ResCompanyDetailDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setDescription(company.getDescription());
        dto.setAddress(company.getAddress());
        dto.setLogo(company.getLogo());

        java.util.List<JobSimpleDTO> jobs = new java.util.ArrayList<>();
        if (company.getJobs() != null) {
            for (Job j : company.getJobs()) {
                jobs.add(JobSimpleDTO.from(j));
            }
        }
        dto.setJobs(jobs);

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/companies")
    public ResponseEntity<Company> getListCompanies(@Valid @RequestBody Company company){
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(this.companyService.HandleUpdateCompany(company));
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Void> getListCompanies(@PathVariable Long id){
        this.companyService.deleteById(id);
         return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/companies/jobs/{id}")
    public ResponseEntity<List<Job>> getAllJobsByCompany(@PathVariable Long id){
        return ResponseEntity.ok(this.companyService.FetchAllJobsByCompany(id));
    }
}
