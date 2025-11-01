package vn.bxh.jobhunter.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.*;
import vn.bxh.jobhunter.domain.response.JobWithApplicantCountDTO;
import vn.bxh.jobhunter.domain.response.ResJobDTO;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.repository.CompanyRepository;
import vn.bxh.jobhunter.repository.JobRepository;
import vn.bxh.jobhunter.repository.SkillRepository;
import vn.bxh.jobhunter.repository.UserRepository;
import vn.bxh.jobhunter.util.SecurityUtil;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;


    public Long getCurrentUserCompanyId() {
        String username = SecurityUtil.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("User not found"));

        User user = userRepository.findByEmail(username);
        if(user == null){
            throw new RuntimeException("User not found");
        }
        return user.getCompany().getId(); // hoặc user.getCompanyId() nếu là Long
    }

    public void DeleteById(long id) {
        Job job = this.jobRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Job does not exist!"));

        // Xóa quan hệ giữa Job và Skill một cách an toàn
        if (job.getSkills() != null && !job.getSkills().isEmpty()) {
            job.getSkills().clear(); // Xóa hết liên kết, không cần vòng lặp
        }

        // Xóa liên kết Resume
        if (job.getResumes() != null) {
            for (Resume resume : new ArrayList<>(job.getResumes())) {
                resume.setJob(null); // Ngắt quan hệ từ Resume
            }
            job.getResumes().clear();
        }

        this.jobRepository.delete(job);
    }
    public List<JobWithApplicantCountDTO> getAllJobsWithApplicantCountByCurrentUser() {
        Long companyId = getCurrentUserCompanyId(); // Tùy bạn implement
        return jobRepository.findAllWithApplicantCountByCompanyId(companyId);
    }


    public ResultPaginationDTO FindAllJobs(Specification<Job> spec, Pageable pageable){
        Page<Job> pageJob = this.jobRepository.findAll(spec,pageable);
        List<Job> jobs = pageJob.getContent();

        List<ResJobDTO> resJobDTOs = jobs.stream().map(job -> {
            ResJobDTO dto = new ResJobDTO();
            dto.setId(job.getId());
            dto.setName(job.getName());
            dto.setLocation(job.getLocation());
            dto.setSalary(job.getSalary());
            dto.setQuantity(job.getQuantity());
            dto.setLevel(job.getLevel());
            dto.setDescription(job.getDescription());
            dto.setStartDate(job.getStartDate());
            dto.setEndDate(job.getEndDate());
            dto.setActive(job.isActive());
            dto.setCreatedAt(job.getCreatedAt());
            dto.setUpdatedAt(job.getUpdatedAt());
            if (job.getSkills() != null) {
                dto.setSkills(job.getSkills().stream().map(Skill::getName).collect(Collectors.toList()));
            }
            if (job.getCompany() != null) {
                ResJobDTO.ResCompanyDTO companyDTO = new ResJobDTO.ResCompanyDTO();
                companyDTO.setId(job.getCompany().getId());
                companyDTO.setName(job.getCompany().getName());
                companyDTO.setLogo(job.getCompany().getLogo());
                dto.setCompany(companyDTO);
            }
            return dto;
        }).collect(Collectors.toList());

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageJob.getNumber()+1,
                pageJob.getSize(),
                pageJob.getTotalPages(),
                pageJob.getTotalElements()
        );
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(resJobDTOs);
        return resultPaginationDTO;
    }

    public Job FindJobById(long id){
        Optional<Job> jobOptional = this.jobRepository.findById(id);
        if(jobOptional.isPresent()){
            return jobOptional.get();
        }
        throw new IdInvalidException("Job dose not exist!");
    }

    public Job HandleSaveJob(Job job){
        Optional<Job> jobOptional = this.jobRepository.findByName(job.getName());
        if(jobOptional.isEmpty()){
            List<Skill> list = new ArrayList<>();
            for (Skill skill : (job.getSkills()==null)?new ArrayList<Skill>():job.getSkills()) {
                Optional<Skill> skillDB = this.skillRepository.findById(skill.getId());
                if(skillDB.isPresent()){
                    list.add(skillDB.get());
                }
            }
            job.setSkills(list);
            if(job.getCompany()!=null){
                Optional<Company> company = this.companyRepository.findById(job.getCompany().getId());
                company.ifPresent(job::setCompany);
            }

            return this.jobRepository.save(job);
        }throw new IdInvalidException("Name is not valid!");
    }



    public Job HandleUpdateJob(Job job){
        Optional<Job> jobOptional = this.jobRepository.findById(job.getId());
        if(jobOptional.isPresent()){
            Job newjob = jobOptional.get();
            newjob.setName(job.getName());
            newjob.setCompany(job.getCompany());
            newjob.setActive(job.isActive());
            newjob.setDescription(job.getDescription());
            newjob.setLevel(job.getLevel());
            newjob.setLocation(job.getLocation());
            newjob.setSalary(job.getSalary());
            newjob.setSkills(job.getSkills());
            return this.jobRepository.save(newjob);

        }
        return null;
    }

    // Trả về danh sách job đơn giản (id, name) theo công ty hiện tại
    public List<vn.bxh.jobhunter.domain.response.JobSimpleDTO> getJobsByCurrentCompanySimple() {
        String username = SecurityUtil.getCurrentUserLogin().orElse(null);
        if (username == null) return new ArrayList<>();

        User user = userRepository.findByEmail(username);
        if (user == null || user.getCompany() == null) return new ArrayList<>();

        Long companyId = user.getCompany().getId();
        List<Job> jobs = jobRepository.findByCompany_Id(companyId);
        if (jobs == null || jobs.isEmpty()) return new ArrayList<>();

        List<vn.bxh.jobhunter.domain.response.JobSimpleDTO> result = new ArrayList<>();
        for (Job j : jobs) {
            result.add(vn.bxh.jobhunter.domain.response.JobSimpleDTO.from(j));
        }
        return result;
    }
}
