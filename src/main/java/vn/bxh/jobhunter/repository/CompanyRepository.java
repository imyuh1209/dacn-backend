package vn.bxh.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.bxh.jobhunter.domain.Company;

public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {

    org.springframework.data.domain.Page<vn.bxh.jobhunter.domain.Company> findByLogoIsNotNull(org.springframework.data.domain.Pageable pageable);

}
