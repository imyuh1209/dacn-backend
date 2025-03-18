package vn.bxh.jobhunter.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.bxh.jobhunter.domain.Permission;
import vn.bxh.jobhunter.domain.Role;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> , JpaSpecificationExecutor<Permission> {
    Optional<Permission> findByName(String name);
}
