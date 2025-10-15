package vn.bxh.jobhunter.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.Skill;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.service.SkillService;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class    SkillController {

    private final SkillService service;

    @PostMapping("/skills")
    public ResponseEntity<Skill> addSkill(@Valid @RequestBody Skill skill){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.HandleSaveSkill(skill));
    }

    @PutMapping("/skills")
    public ResponseEntity<Skill> updateSkill(@Valid @RequestBody Skill skill){
        return ResponseEntity.ok(this.service.HandleUpdateSkill(skill));
    }

    @GetMapping("/skills/{id}")
    public ResponseEntity<Skill> getSkill(@PathVariable Long id){
        return ResponseEntity.ok(this.service.GetSkill(id));
    }

    @GetMapping("/skills")
    public ResponseEntity<ResultPaginationDTO> getAllSkill(@Filter Specification<Skill> spec, Pageable pageable){
        return ResponseEntity.ok(this.service.FindAllSkills(spec, pageable));
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id){
        this.service.HandleDeleteSkill(id);
        return ResponseEntity.ok(null);
    }
}
