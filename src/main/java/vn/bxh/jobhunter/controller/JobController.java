package vn.bxh.jobhunter.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
        dto.setSalaryMin(job.getSalaryMin());
        dto.setSalaryMax(job.getSalaryMax());
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

    // Tìm kiếm theo từ khóa q (tên job, vị trí, tên công ty),
    // và lọc theo các tham số nâng cao: location, company, minSalary, maxSalary
    @GetMapping("/jobs/search")
    public ResponseEntity<ResultPaginationDTO> searchJobs(@RequestParam(name = "q", required = false) String q,
                                                          @RequestParam(name = "keyword", required = false) String keyword,
                                                          @RequestParam(name = "location", required = false) String location,
                                                          @RequestParam(name = "loc", required = false) String loc,
                                                          @RequestParam(name = "company", required = false) String company,
                                                          @RequestParam(name = "companyName", required = false) String companyName,
                                                          @RequestParam(name = "minSalary", required = false) Double minSalary,
                                                          @RequestParam(name = "min_salary", required = false) Double minSalaryAlt,
                                                          @RequestParam(name = "maxSalary", required = false) Double maxSalary,
                                                          @RequestParam(name = "max_salary", required = false) Double maxSalaryAlt,
                                                          Pageable pageable,
                                                          @RequestParam(name = "page", required = false) Integer page,
                                                          @RequestParam(name = "pageIndex", required = false) Integer pageIndex,
                                                          @RequestParam(name = "size", required = false) Integer size,
                                                          @RequestParam(name = "pageSize", required = false) Integer pageSize) {
        String termRaw = (q != null ? q : keyword);
        String locationRaw = (location != null ? location : loc);
        String companyRaw = (company != null ? company : companyName);
        Double minSalaryRaw = (minSalary != null ? minSalary : minSalaryAlt);
        Double maxSalaryRaw = (maxSalary != null ? maxSalary : maxSalaryAlt);
        Specification<Job> spec = (root, query, cb) -> {
            String term = (termRaw == null) ? "" : termRaw.trim().toLowerCase();
            String locationTerm = (locationRaw == null) ? "" : locationRaw.trim().toLowerCase();
            String companyTerm = (companyRaw == null) ? "" : companyRaw.trim().toLowerCase();

            // Tập hợp các điều kiện
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            // Điều kiện theo từ khóa
            if (!term.isEmpty()) {
                jakarta.persistence.criteria.Join<vn.bxh.jobhunter.domain.Job, vn.bxh.jobhunter.domain.Company> companyJoin =
                        root.join("company", jakarta.persistence.criteria.JoinType.LEFT);
                jakarta.persistence.criteria.Predicate byJobName = cb.like(cb.lower(root.get("name")), "%" + term + "%");
                jakarta.persistence.criteria.Predicate byLocation = cb.like(cb.lower(root.get("location")), "%" + term + "%");
                jakarta.persistence.criteria.Predicate byCompanyName = cb.like(cb.lower(companyJoin.get("name")), "%" + term + "%");
                // Gộp điều kiện text bằng OR thành một predicate
                predicates.add(cb.or(byJobName, byLocation, byCompanyName));
            }

            // Điều kiện theo location riêng
            if (!locationTerm.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + locationTerm + "%"));
            }

            // Điều kiện theo company riêng
            if (!companyTerm.isEmpty()) {
                jakarta.persistence.criteria.Join<vn.bxh.jobhunter.domain.Job, vn.bxh.jobhunter.domain.Company> companyJoin =
                        root.join("company", jakarta.persistence.criteria.JoinType.LEFT);
                predicates.add(cb.like(cb.lower(companyJoin.get("name")), "%" + companyTerm + "%"));
            }

            // Điều kiện theo khoảng lương: 
            // - minSalary: job.salaryMax >= minSalary
            // - maxSalary: job.salaryMin <= maxSalary
            if (minSalaryRaw != null) {
                // dùng coalesce để tương thích dữ liệu cũ (null -> salary)
                jakarta.persistence.criteria.Expression<Double> maxExpr = cb.coalesce(root.get("salaryMax"), root.get("salary"));
                predicates.add(cb.greaterThanOrEqualTo(maxExpr, minSalaryRaw));
            }
            if (maxSalaryRaw != null) {
                // dùng coalesce để tương thích dữ liệu cũ (null -> salary)
                jakarta.persistence.criteria.Expression<Double> minExpr = cb.coalesce(root.get("salaryMin"), root.get("salary"));
                predicates.add(cb.lessThanOrEqualTo(minExpr, maxSalaryRaw));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        // Chuẩn hóa page/size: UI gửi 1-based, chuyển về 0-based
        int pageIndexEff = (page != null) ? Math.max(page - 1, 0)
                : (pageIndex != null ? Math.max(pageIndex, 0) : pageable.getPageNumber());
        int pageSizeEff = (size != null) ? size
                : (pageSize != null ? pageSize : pageable.getPageSize());
        Sort sort = pageable.getSort().isUnsorted() ? Sort.by(Sort.Direction.DESC, "createdAt") : pageable.getSort();
        Pageable effectivePageable = PageRequest.of(pageIndexEff, pageSizeEff, sort);

        ResultPaginationDTO result = this.jobService.FindAllJobs(spec, effectivePageable);
        return ResponseEntity.ok(result);
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
