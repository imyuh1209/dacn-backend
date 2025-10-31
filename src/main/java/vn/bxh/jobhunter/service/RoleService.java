package vn.bxh.jobhunter.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.Permission;
import vn.bxh.jobhunter.domain.Role;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.repository.PermissionRepository;
import vn.bxh.jobhunter.repository.RoleRepository;
import vn.bxh.jobhunter.util.error.IdInvalidException;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RoleService {
    public final RoleRepository roleRepository;
    public final PermissionRepository permissionRepository;

    public Role CreateRole(Role role){
        Optional<Role> roleOptional = this.roleRepository.findByName(role.getName());
        if(roleOptional.isEmpty()){
            // Nếu tạo SUPER_ADMIN, mặc định full quyền, bỏ qua payload
            if("SUPER_ADMIN".equalsIgnoreCase(role.getName())){
                role.setPermissions(this.permissionRepository.findAll());
            } else if(role.getPermissions()!=null){
                List<Permission> listPer = new ArrayList<>();
                for (Permission per : role.getPermissions()){
                    Optional<Permission> permission = this.permissionRepository.findById(per.getId());
                    permission.ifPresent(listPer::add);// add to list
                }
                role.setPermissions(listPer);
            }
            return this.roleRepository.save(role);
        }throw new IdInvalidException("Create not success!");
    }



    public Role UpdateRole(Role role){
        if(this.roleRepository.existsByIdAndName(role.getId(), role.getName())){
            Role roleDB = this.roleRepository.findById(role.getId()).get();
            roleDB.setActive(role.isActive());
            // Nếu là SUPER_ADMIN: luôn full quyền
            if("SUPER_ADMIN".equalsIgnoreCase(roleDB.getName())){
                roleDB.setPermissions(this.permissionRepository.findAll());
            } else {
                // Ghi đè danh sách quyền theo payload từ client (không cộng dồn)
                if(role.getPermissions()!=null){
                    List<Long> ids = new ArrayList<>();
                    for (Permission per : role.getPermissions()) {
                        ids.add(per.getId());
                    }
                    List<Long> distinctIds = ids.stream().distinct().toList();
                    List<Permission> newPermissions = this.permissionRepository.findAllById(distinctIds);
                    roleDB.setPermissions(newPermissions);
                } else {
                    roleDB.setPermissions(new ArrayList<>());
                }
            }
            roleDB.setDescription(role.getDescription());
            roleDB.setName(role.getName());
            return this.roleRepository.save(roleDB);
        }throw new IdInvalidException("Update not success!");
    }

    public void DeleteRole(long id){
        Optional<Role> roleOptional = this.roleRepository.findById(id);
        if(roleOptional.isPresent()){
            this.roleRepository.deleteById(id);
        }else{
            throw new IdInvalidException("Id dose not exist !");
        }
    }

    public Role GetRole(long id){
        Optional<Role> roleOptional = this.roleRepository.findById(id);
        if(roleOptional.isPresent()){
            return roleOptional.get();
        }else{
            throw new IdInvalidException("Id dose not exist !");
        }
    }

    public ResultPaginationDTO GetAllRole(Specification<Role> spec, Pageable pageable){
        Page<Role> rolePage = this.roleRepository.findAll(spec,pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(rolePage.getNumber()+1, rolePage.getSize(), rolePage.getTotalPages(), rolePage.getTotalElements());
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(rolePage.getContent());
        return resultPaginationDTO;
    }
}
