package vn.bxh.jobhunter.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.*;
import vn.bxh.jobhunter.domain.response.JobWithApplicantCountDTO;
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
        Page<Job> page = this.jobRepository.findAll(spec,pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                page.getNumber()+1,
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements()
        );
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(page.getContent());
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
}
