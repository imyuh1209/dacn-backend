package vn.bxh.jobhunter.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.Company;
import vn.bxh.jobhunter.domain.dto.Meta;
import vn.bxh.jobhunter.domain.dto.ResultPaginationDTO;
import vn.bxh.jobhunter.repository.CompanyRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;

    public Company HandleSaveCompany(Company company){
        return this.companyRepository.save(company);
    }

    public Company HandleUpdateCompany(Company company){
        Optional<Company> updateCompany = this.FindById(company.getId());
        if(updateCompany.isPresent()){
            Company c = updateCompany.get();
            c.setLogo(company.getLogo());
            c.setName(company.getName());
            c.setDescription(company.getDescription());
            c.setAddress(company.getAddress());
            return c;
        }
        return null;

    }

    public ResultPaginationDTO FetchAllCompanies(Specification<Company> spec, Pageable pageable){
        Page<Company> page = this.companyRepository.findAll(spec,pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        Meta meta = new Meta(page.getNumber()+1,page.getSize(), page.getTotalPages(), page.getTotalElements());
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(page.getContent());
        return resultPaginationDTO;
    }

    public void deleteById(Long id){
        this.companyRepository.deleteById(id);
    }

    public Optional<Company> FindById(long id){
        return this.companyRepository.findById(id);
    }
}
