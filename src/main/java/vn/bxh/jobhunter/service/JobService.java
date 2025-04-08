package vn.bxh.jobhunter.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.Company;
import vn.bxh.jobhunter.domain.Job;
import vn.bxh.jobhunter.domain.Skill;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.repository.CompanyRepository;
import vn.bxh.jobhunter.repository.JobRepository;
import vn.bxh.jobhunter.repository.SkillRepository;
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

    public void DeleteById(long id){
        Optional<Job> jobOptional = this.jobRepository.findById(id);
        if(jobOptional.isPresent()){
            Job jobDelete = jobOptional.get();
            for (Skill skill : jobDelete.getSkills()==null? new ArrayList<Skill>():jobDelete.getSkills()){
                jobDelete.getSkills().remove(skill);
            }
            this.jobRepository.delete(jobDelete);
        }else{
            throw new IdInvalidException("Job dose not exist!");
        }
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
