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
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.Optional;

@AllArgsConstructor
@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public void DeletePermission(long id){
        Optional<Permission> permissionOptional = this.permissionRepository.findById(id);
        if(permissionOptional.isPresent()){
            Permission permission = permissionOptional.get();

            for (Role role : permission.getRoles()) {
                role.getPermissions().remove(permission);
            }
           // permission.getRoles().forEach(role -> role.getPermissions().remove(permission));


            this.permissionRepository.delete(permission);
        }else{
            throw new IdInvalidException("Id is not valid");
        }
    }

    public Permission GetPermission(long id){
        Optional<Permission> permissionOptional = this.permissionRepository.findById(id);
        if(permissionOptional.isPresent()){
            return permissionOptional.get();
        }throw new IdInvalidException("Id is not valid");
    }

    public Permission HandleUpdatePermission(Permission permission){
        Optional<Permission> permissionOptional = this.permissionRepository.findById(permission.getId());
        if(permissionOptional.isPresent()){
            Permission permissionDB = permissionOptional.get();
            permissionDB.setName(permission.getName());
            permissionDB.setApiPath(permission.getApiPath());
            permissionDB.setMethod(permission.getMethod());
            permissionDB.setModule(permission.getModule());
            return this.permissionRepository.save(permissionDB);
        }throw new IdInvalidException("Id is not valid !");
    }

    public Permission HandleCreatePermission(Permission permission){
        Optional<Permission> permissionOptional = this.permissionRepository.findByName(permission.getName());
        if(permissionOptional.isEmpty()){
            return this.permissionRepository.save(permission);
        }throw new IdInvalidException("Permission name is not valid!");
    }

    public ResultPaginationDTO GetAllPermission(Specification<Permission> spec, Pageable pageable) {
        Page<Permission> rolePage = this.permissionRepository.findAll(spec,pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(rolePage.getNumber()+1, rolePage.getSize(), rolePage.getTotalPages(), rolePage.getTotalElements());
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(rolePage.getContent());
        return resultPaginationDTO;
    }
}
