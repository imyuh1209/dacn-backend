package vn.bxh.jobhunter.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bxh.jobhunter.domain.Job;
import vn.bxh.jobhunter.domain.Skill;
import vn.bxh.jobhunter.domain.Subscriber;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.repository.JobRepository;
import vn.bxh.jobhunter.repository.SkillRepository;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.Optional;

@Service
@AllArgsConstructor
public class SkillService {
    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;

    public ResultPaginationDTO FindAllSkills(Specification<Skill> spec, Pageable pageable) {
        Page<Skill> skillPage = this.skillRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                skillPage.getNumber() + 1,
                skillPage.getSize(),
                skillPage.getTotalPages(),
                skillPage.getTotalElements()
        );
        result.setMeta(meta);
        result.setResult(skillPage); // giữ nguyên: frontend đang đọc result.content
        return result;
    }

    @Transactional
    public void HandleDeleteSkill(Long id) {
        Skill skill = this.skillRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Id does not exist!"));

        // Gỡ quan hệ ManyToMany (owning side thường nằm ở Job/Subscriber)
        if (skill.getJobs() != null) {
            for (Job job : skill.getJobs()) {
                job.getSkills().remove(skill);
            }
        }
        if (skill.getSubscribers() != null) {
            for (Subscriber sub : skill.getSubscribers()) {
                sub.getSkills().remove(skill);
            }
        }

        // Xoá skill
        this.skillRepository.delete(skill);
        // @Transactional + dirty checking sẽ persist thay đổi quan hệ
    }

    public Skill GetSkill(Long id) {
        return this.skillRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Id does not exist!"));
    }

    public Skill HandleSaveSkill(Skill s) {
        s.setId(null);                       // ép null khi create
        String name = normalize(s.getName());
        if (name == null || name.length() < 3) {
            throw new IdInvalidException("Name must have at least 3 characters");
        }
        if (skillRepository.findByName(name).isPresent()) {
            throw new IdInvalidException("Skill name already exists");
        }
        s.setName(name);
        return skillRepository.save(s);
    }

    public Skill HandleUpdateSkill(Skill s) {
        if (s.getId() == null) throw new IdInvalidException("Id must not be null for update");
        Skill cur = skillRepository.findById(s.getId())
                .orElseThrow(() -> new IdInvalidException("Id does not exist"));

        String name = normalize(s.getName());
        if (name == null || name.length() < 3) {
            throw new IdInvalidException("Name must have at least 3 characters");
        }
        skillRepository.findByName(name).ifPresent(other -> {
            if (!other.getId().equals(cur.getId())) {
                throw new IdInvalidException("Skill name already exists");
            }
        });
        cur.setName(name);
        return skillRepository.save(cur);
    }

    private String normalize(String raw) {
        if (raw == null) return null;
        String s = raw.trim().replaceAll("\\s+", " ");
        return s.isEmpty() ? null : s;
    }
}
