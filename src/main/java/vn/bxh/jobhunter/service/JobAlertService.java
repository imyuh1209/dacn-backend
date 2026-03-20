package vn.bxh.jobhunter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.Job;
import vn.bxh.jobhunter.domain.JobAlert;
import vn.bxh.jobhunter.domain.request.ReqJobAlertUpsert;
import vn.bxh.jobhunter.repository.JobAlertRepository;
import vn.bxh.jobhunter.repository.JobRepository;
import vn.bxh.jobhunter.util.Constant.JobAlertFrequencyEnum;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobAlertService {

    private final JobAlertRepository jobAlertRepository;
    private final JobRepository jobRepository;
    private final EmailService emailService;

    public JobAlert create(ReqJobAlertUpsert req) {
        JobAlert alert = new JobAlert();
        String normalizedEmail = req.getEmail() == null ? null : req.getEmail().trim();
        alert.setEmail(normalizedEmail);
        alert.setKeyword(safe(req.getKeyword()));
        alert.setLocation(safe(req.getLocation()));
        alert.setSalaryMin(req.getSalaryMin());
        alert.setSalaryMax(req.getSalaryMax());
        alert.setLevel(safe(req.getLevel()));
        alert.setCompanyName(safe(req.getCompanyName()));
        alert.setEnabled(Boolean.TRUE.equals(req.getEnabled()));
        try {
            alert.setFrequency(JobAlertFrequencyEnum.valueOf(req.getFrequency().trim().toUpperCase()));
        } catch (Exception e) {
            throw new IdInvalidException("Tần suất không hợp lệ");
        }
        alert.setCreatedAt(Instant.now());
        return jobAlertRepository.save(alert);
    }

    private String safe(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }

    public List<Job> findMatchingJobs(JobAlert alert) {
        Specification<Job> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (alert.getKeyword() != null) {
                var companyJoin = root.join("company", jakarta.persistence.criteria.JoinType.LEFT);
                var byJobName = cb.like(cb.lower(root.get("name")), "%" + alert.getKeyword().toLowerCase() + "%");
                var byLocation = cb.like(cb.lower(root.get("location")), "%" + alert.getKeyword().toLowerCase() + "%");
                var byCompanyName = cb.like(cb.lower(companyJoin.get("name")), "%" + alert.getKeyword().toLowerCase() + "%");
                predicates.add(cb.or(byJobName, byLocation, byCompanyName));
            }
            if (alert.getLocation() != null) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + alert.getLocation().toLowerCase() + "%"));
            }
            if (alert.getCompanyName() != null) {
                var companyJoin = root.join("company", jakarta.persistence.criteria.JoinType.LEFT);
                predicates.add(cb.like(cb.lower(companyJoin.get("name")), "%" + alert.getCompanyName().toLowerCase() + "%"));
            }
            if (alert.getSalaryMin() != null) {
                // so sánh với salaryMax hoặc salary nếu không có max
                var byMax = cb.greaterThanOrEqualTo(root.get("salaryMax"), alert.getSalaryMin());
                var byFixed = cb.greaterThanOrEqualTo(root.get("salary"), alert.getSalaryMin());
                predicates.add(cb.or(byMax, byFixed));
            }
            if (alert.getSalaryMax() != null) {
                // so sánh với salaryMin hoặc salary nếu không có min
                var byMin = cb.lessThanOrEqualTo(root.get("salaryMin"), alert.getSalaryMax());
                var byFixed = cb.lessThanOrEqualTo(root.get("salary"), alert.getSalaryMax());
                predicates.add(cb.or(byMin, byFixed));
            }
            if (predicates.isEmpty()) return cb.conjunction();
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return jobRepository.findAll(spec);
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyAlerts() {
        List<JobAlert> alerts = jobAlertRepository.findAllByEnabledTrue();
        for (JobAlert alert : alerts) {
            if (alert.getFrequency() == JobAlertFrequencyEnum.DAILY) {
                List<vn.bxh.jobhunter.domain.response.Email.ResEmailJob> jobs = this.findMatchingJobs(alert)
                        .stream().map(j -> {
                            vn.bxh.jobhunter.domain.response.Email.ResEmailJob dto = new vn.bxh.jobhunter.domain.response.Email.ResEmailJob();
                            dto.setName(j.getName());
                            dto.setSalary(j.getSalary());
                            dto.setCompany(new vn.bxh.jobhunter.domain.response.Email.ResEmailJob.CompanyEmail(
                                    j.getCompany() != null ? j.getCompany().getName() : ""));
                            java.util.List<vn.bxh.jobhunter.domain.Skill> skills = j.getSkills();
                            java.util.List<vn.bxh.jobhunter.domain.response.Email.ResEmailJob.SkillEmail> s =
                                    skills == null ? java.util.List.of() : skills.stream()
                                            .map(sk -> new vn.bxh.jobhunter.domain.response.Email.ResEmailJob.SkillEmail(sk.getName()))
                                            .collect(java.util.stream.Collectors.toList());
                            dto.setSkills(s);
                            return dto;
                        }).toList();
                emailService.sendEmailFromTemplateSync(alert.getEmail(),
                        "Job alert hằng ngày", "job", alert.getEmail(), jobs);
                alert.setLastSentAt(Instant.now());
                alert.setUpdatedAt(Instant.now());
                jobAlertRepository.save(alert);
            }
        }
    }

    @Scheduled(cron = "0 0 8 ? * MON")
    public void sendWeeklyAlerts() {
        List<JobAlert> alerts = jobAlertRepository.findAllByEnabledTrue();
        for (JobAlert alert : alerts) {
            if (alert.getFrequency() == JobAlertFrequencyEnum.WEEKLY) {
                List<vn.bxh.jobhunter.domain.response.Email.ResEmailJob> jobs = this.findMatchingJobs(alert)
                        .stream().map(j -> {
                            vn.bxh.jobhunter.domain.response.Email.ResEmailJob dto = new vn.bxh.jobhunter.domain.response.Email.ResEmailJob();
                            dto.setName(j.getName());
                            dto.setSalary(j.getSalary());
                            dto.setCompany(new vn.bxh.jobhunter.domain.response.Email.ResEmailJob.CompanyEmail(
                                    j.getCompany() != null ? j.getCompany().getName() : ""));
                            java.util.List<vn.bxh.jobhunter.domain.Skill> skills = j.getSkills();
                            java.util.List<vn.bxh.jobhunter.domain.response.Email.ResEmailJob.SkillEmail> s =
                                    skills == null ? java.util.List.of() : skills.stream()
                                            .map(sk -> new vn.bxh.jobhunter.domain.response.Email.ResEmailJob.SkillEmail(sk.getName()))
                                            .collect(java.util.stream.Collectors.toList());
                            dto.setSkills(s);
                            return dto;
                        }).toList();
                emailService.sendEmailFromTemplateSync(alert.getEmail(),
                        "Job alert hàng tuần", "job", alert.getEmail(), jobs);
                alert.setLastSentAt(Instant.now());
                alert.setUpdatedAt(Instant.now());
                jobAlertRepository.save(alert);
            }
        }
    }

    public java.util.List<JobAlert> listByEmailOrCurrent(String emailOpt) {
        String email = emailOpt;
        if (email == null || email.isBlank()) {
            email = vn.bxh.jobhunter.util.SecurityUtil.getCurrentUserLogin().orElse(null);
        }
        if (email == null || email.isBlank()) {
            return java.util.List.of();
        }
        email = email.trim();
        java.util.List<JobAlert> list = jobAlertRepository.findByEmailNormalized(email);
        if (list == null || list.isEmpty()) {
            list = jobAlertRepository.findByEmailIgnoreCase(email);
        }
        return list;
    }

    public JobAlert updateOwned(String currentEmail, vn.bxh.jobhunter.domain.request.ReqJobAlertUpdate req) {
        if (currentEmail == null || currentEmail.isBlank()) {
            throw new IdInvalidException("Không xác định người dùng");
        }
        JobAlert alert = jobAlertRepository.findById(req.getId())
                .orElseThrow(() -> new IdInvalidException("Id không hợp lệ"));
        if (!currentEmail.equalsIgnoreCase(alert.getEmail())) {
            throw new IdInvalidException("Bạn không có quyền cập nhật job alert này");
        }
        if (req.getKeyword() != null) alert.setKeyword(safe(req.getKeyword()));
        if (req.getLocation() != null) alert.setLocation(safe(req.getLocation()));
        if (req.getCompanyName() != null) alert.setCompanyName(safe(req.getCompanyName()));
        if (req.getLevel() != null) alert.setLevel(safe(req.getLevel()));
        if (req.getSalaryMin() != null) alert.setSalaryMin(req.getSalaryMin());
        if (req.getSalaryMax() != null) alert.setSalaryMax(req.getSalaryMax());
        if (req.getEnabled() != null) alert.setEnabled(req.getEnabled());
        if (req.getFrequency() != null) {
            try {
                alert.setFrequency(JobAlertFrequencyEnum.valueOf(req.getFrequency().trim().toUpperCase()));
            } catch (Exception e) {
                throw new IdInvalidException("Tần suất không hợp lệ");
            }
        }
        alert.setUpdatedAt(Instant.now());
        return jobAlertRepository.save(alert);
    }

    public void deleteOwned(String currentEmail, Long id) {
        if (currentEmail == null || currentEmail.isBlank()) {
            throw new IdInvalidException("Không xác định người dùng");
        }
        JobAlert alert = jobAlertRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Id không hợp lệ"));
        if (!currentEmail.equalsIgnoreCase(alert.getEmail())) {
            throw new IdInvalidException("Bạn không có quyền xóa job alert này");
        }
        jobAlertRepository.delete(alert);
    }

    public void runNowOwned(String currentEmail, Long id) {
        if (currentEmail == null || currentEmail.isBlank()) {
            throw new IdInvalidException("Không xác định người dùng");
        }
        JobAlert alert = jobAlertRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Id không hợp lệ"));
        if (!currentEmail.equalsIgnoreCase(alert.getEmail())) {
            throw new IdInvalidException("Bạn không có quyền thực thi job alert này");
        }
        java.util.List<vn.bxh.jobhunter.domain.response.Email.ResEmailJob> jobs = this.findMatchingJobs(alert)
                .stream().map(j -> {
                    vn.bxh.jobhunter.domain.response.Email.ResEmailJob dto = new vn.bxh.jobhunter.domain.response.Email.ResEmailJob();
                    dto.setName(j.getName());
                    dto.setSalary(j.getSalary());
                    dto.setCompany(new vn.bxh.jobhunter.domain.response.Email.ResEmailJob.CompanyEmail(
                            j.getCompany() != null ? j.getCompany().getName() : ""));
                    java.util.List<vn.bxh.jobhunter.domain.Skill> skills = j.getSkills();
                    java.util.List<vn.bxh.jobhunter.domain.response.Email.ResEmailJob.SkillEmail> s =
                            skills == null ? java.util.List.of() : skills.stream()
                                    .map(sk -> new vn.bxh.jobhunter.domain.response.Email.ResEmailJob.SkillEmail(sk.getName()))
                                    .collect(java.util.stream.Collectors.toList());
                    dto.setSkills(s);
                    return dto;
                }).toList();
        emailService.sendEmailFromTemplateSync(alert.getEmail(),
                "Job alert", "job", alert.getEmail(), jobs);
        alert.setLastSentAt(Instant.now());
        alert.setUpdatedAt(Instant.now());
        jobAlertRepository.save(alert);
    }
}
