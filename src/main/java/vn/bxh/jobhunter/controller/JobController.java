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
                                                          @RequestParam(name = "category", required = false) String category,
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
                                                          @RequestParam(name = "pageSize", required = false) Integer pageSize,
                                                          @RequestParam(name = "sort", required = false) String sortParam) {
        String termRaw = (q != null ? q : keyword);
        String categoryRaw = category;
        String locationRaw = (location != null ? location : loc);
        String companyRaw = (company != null ? company : companyName);
        Double minSalaryRaw = (minSalary != null ? minSalary : minSalaryAlt);
        Double maxSalaryRaw = (maxSalary != null ? maxSalary : maxSalaryAlt);

        // [SECURITY] Sanitization: Lọc bỏ thẻ <script>
        if (termRaw != null) termRaw = termRaw.replaceAll("<script.*?>.*?</script>", "").replaceAll("<.*?>", "");
        if (categoryRaw != null) categoryRaw = categoryRaw.replaceAll("<.*?>", "");
        if (locationRaw != null) locationRaw = locationRaw.replaceAll("<.*?>", "");
        if (companyRaw != null) companyRaw = companyRaw.replaceAll("<.*?>", "");

        // [LOGIC] Cải thiện Semantic Search: Tự động phát hiện "lương", địa điểm, hoặc level trong q
        vn.bxh.jobhunter.util.Constant.LevelEnum detectedLevel = null;
        if (termRaw != null) {
            String lowerTerm = termRaw.toLowerCase();
            
            // 1. Detect Lương (ví dụ: "lương > 20tr", "lương 15 triệu")
            if (lowerTerm.contains("lương") || lowerTerm.contains("luong")) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)").matcher(lowerTerm);
                if (m.find()) {
                    double val = Double.parseDouble(m.group(1));
                    if (lowerTerm.contains("tr") || lowerTerm.contains("triệu") || lowerTerm.contains("trieu")) {
                        val = val * 1000000;
                    }
                    if (minSalaryRaw == null) minSalaryRaw = val;
                } else if (lowerTerm.contains("cao")) {
                    if (minSalaryRaw == null) minSalaryRaw = 1000.0; // Mặc định lương cao > 1000$
                }
            }

            // 2. Tự động bóc tách địa điểm và chuẩn hóa mã (HANOI, HCM...)
            if (lowerTerm.contains("hà nội") || lowerTerm.contains("ha noi")) {
                if (locationRaw == null) locationRaw = "HANOI";
            } else if (lowerTerm.contains("hồ chí minh") || lowerTerm.contains("ho chi minh") || lowerTerm.contains("hcm")) {
                if (locationRaw == null) locationRaw = "HCM";
            } else if (lowerTerm.contains("đà nẵng") || lowerTerm.contains("da nang")) {
                if (locationRaw == null) locationRaw = "DANANG";
            } else if (lowerTerm.contains("cần thơ") || lowerTerm.contains("can tho")) {
                if (locationRaw == null) locationRaw = "CANTHO";
            }

            // 3. Tự động phát hiện Level
            if (lowerTerm.contains("intern") || lowerTerm.contains("thực tập")) detectedLevel = vn.bxh.jobhunter.util.Constant.LevelEnum.INTERN;
            else if (lowerTerm.contains("fresher")) detectedLevel = vn.bxh.jobhunter.util.Constant.LevelEnum.FRESHER;
            else if (lowerTerm.contains("junior")) detectedLevel = vn.bxh.jobhunter.util.Constant.LevelEnum.JUNIOR;
            else if (lowerTerm.contains("middle")) detectedLevel = vn.bxh.jobhunter.util.Constant.LevelEnum.MIDDLE;
            else if (lowerTerm.contains("senior")) detectedLevel = vn.bxh.jobhunter.util.Constant.LevelEnum.SENIOR;
        }

        final String finalTermRaw = termRaw;
        final String finalCategoryRaw = categoryRaw;
        final String finalLocationRaw = locationRaw;
        final String finalCompanyRaw = companyRaw;
        final Double finalMinSalaryRaw = minSalaryRaw;
        final Double finalMaxSalaryRaw = maxSalaryRaw;
        final vn.bxh.jobhunter.util.Constant.LevelEnum finalDetectedLevel = detectedLevel;

        Specification<Job> spec = (root, query, cb) -> {
            String term = (finalTermRaw == null) ? "" : finalTermRaw.trim().toLowerCase();
            String categoryTerm = (finalCategoryRaw == null) ? "" : finalCategoryRaw.trim().toLowerCase();
            String locationTerm = (finalLocationRaw == null) ? "" : finalLocationRaw.trim().toLowerCase();
            String companyTerm = (finalCompanyRaw == null) ? "" : finalCompanyRaw.trim().toLowerCase();

            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            // Điều kiện theo từ khóa (nếu không phải là level đơn lẻ)
            if (!term.isEmpty()) {
                jakarta.persistence.criteria.Join<vn.bxh.jobhunter.domain.Job, vn.bxh.jobhunter.domain.Company> companyJoin =
                        root.join("company", jakarta.persistence.criteria.JoinType.LEFT);
                
                jakarta.persistence.criteria.Predicate byJobName = cb.like(cb.lower(root.get("name")), "%" + term + "%");
                jakarta.persistence.criteria.Predicate byLocation = cb.like(cb.lower(root.get("location")), "%" + term + "%");
                jakarta.persistence.criteria.Predicate byCompanyName = cb.like(cb.lower(companyJoin.get("name")), "%" + term + "%");
                
                // Nếu detect được level, thêm điều kiện level vào
                if (finalDetectedLevel != null) {
                    predicates.add(cb.equal(root.get("level"), finalDetectedLevel));
                } else {
                    predicates.add(cb.or(byJobName, byLocation, byCompanyName));
                }
            }

            // Điều kiện theo category (nếu UI dùng category thay cho q)
            if (!categoryTerm.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + categoryTerm + "%"));
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

            if (finalMinSalaryRaw != null) {
                jakarta.persistence.criteria.Expression<Double> maxExpr = cb.coalesce(root.get("salaryMax"), root.get("salary"));
                predicates.add(cb.greaterThanOrEqualTo(maxExpr, finalMinSalaryRaw));
            }
            if (finalMaxSalaryRaw != null) {
                jakarta.persistence.criteria.Expression<Double> minExpr = cb.coalesce(root.get("salaryMin"), root.get("salary"));
                predicates.add(cb.lessThanOrEqualTo(minExpr, finalMaxSalaryRaw));
            }

            // [PRIORITY 1] Fix lỗi không xóa kết quả cũ khi tìm kiếm rỗng
            if (finalTermRaw != null && finalTermRaw.trim().isEmpty() && predicates.isEmpty()) {
                // Trả về kết quả rỗng nếu search rỗng
                // predicates.add(cb.disjunction());
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        // [API] Xử lý tham số Page & Size: Đảm bảo UI 1-based -> BE 0-based
        int pageIndexEff = (page != null) ? Math.max(page - 1, 0)
                : (pageIndex != null ? Math.max(pageIndex - 1, 0) : pageable.getPageNumber());
        int pageSizeEff = (size != null) ? size
                : (pageSize != null ? pageSize : pageable.getPageSize());

        // [PRIORITY 2] Bổ sung tính năng Sắp xếp (Sort)
        Sort sort = pageable.getSort();
        if (sortParam != null && !sortParam.isEmpty()) {
            String[] parts = sortParam.split(",");
            String field = parts[0];
            Sort.Direction direction = (parts.length > 1 && parts[1].equalsIgnoreCase("asc")) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, field);
        } else if (sort.isUnsorted()) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

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
