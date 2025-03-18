package vn.bxh.jobhunter.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.Role;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.service.RoleService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class RoleController {
    private final RoleService roleService;

    @PostMapping("/roles")
    public ResponseEntity<Role> CreateRole(@Valid @RequestBody Role role){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.roleService.CreateRole(role));
    }

    @PutMapping("/roles")
    public ResponseEntity<Role> UpdateRole(@Valid @RequestBody Role role){
        return ResponseEntity.ok(this.roleService.UpdateRole(role));
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<Role> GetRole(@Valid @PathVariable long id){
        return ResponseEntity.ok(this.roleService.GetRole(id));
    }

    @DeleteMapping("roles/{id}")
    public ResponseEntity<Void> DeletePermission(@PathVariable long id){
        this.roleService.DeleteRole(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/roles")
    public ResponseEntity<ResultPaginationDTO> GetAllRole(@Filter Specification<Role> spec, Pageable pageable){
        return ResponseEntity.ok(this.roleService.GetAllRole(spec, pageable));
    }
}
