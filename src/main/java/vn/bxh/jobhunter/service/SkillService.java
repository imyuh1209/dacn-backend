package vn.bxh.jobhunter.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.Job;
import vn.bxh.jobhunter.domain.Skill;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.response.ResUserDTO;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.repository.JobRepository;
import vn.bxh.jobhunter.repository.SkillRepository;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SkillService {
    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    
    public ResultPaginationDTO FindAllSkills(Specification<Skill> spec, Pageable pageable){
        Page<Skill> skillPage = this.skillRepository.findAll(spec,pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                skillPage.getNumber()+1, skillPage.getSize(), skillPage.getTotalPages(), skillPage.getTotalElements());
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(skillPage);
        return resultPaginationDTO;
    } 

    public void HandleDeleteSkill(long id){
        Optional<Skill> skillOptional = this.skillRepository.findById(id);
        if(skillOptional.isPresent()){
            Skill skill = skillOptional.get();
            // Xóa tất cả quan hệ giữa Skill và Job trong bảng trung gian
            for (Job job : skill.getJobs()) {
                job.getSkills().remove(skill);
            }
            // Lưu lại các thay đổi
            this.jobRepository.saveAll(skill.getJobs());
            // Xóa Skill
            this.skillRepository.deleteById(id);
        }else{
            throw new IdInvalidException("Id dose not exist!");
        }

    }

    public Skill GetSkill(long id){
        Optional<Skill> skillOptional = this.skillRepository.findById(id);
        if(skillOptional.isPresent()){
            return skillOptional.get();
        }
        throw new IdInvalidException("Id does not exist!");
    }

    public Skill HandleSaveSkill(Skill skill){
        Optional<Skill> skillOptional = this.skillRepository.findByName(skill.getName());
        if (skillOptional.isPresent()){
            throw new IdInvalidException("Name skill is not valid!");
        }
        return this.skillRepository.save(skill);
    }

    public Skill HandleUpdateSkill(Skill skill){
        Optional<Skill> skillOptional = this.skillRepository.findByName(skill.getName());
        if (skillOptional.isPresent()){
            throw new IdInvalidException("Name or Id is not valid!");
        }
        Optional<Skill> skillOptionalId = this.skillRepository.findById(skill.getId());
        if (skillOptionalId.isPresent()){
            Skill newSkill = skillOptionalId.get();
            newSkill.setName(skill.getName());
            return this.skillRepository.save(newSkill);
        }else{
            throw new IdInvalidException("ID does not exist ");
        }

    }
}
