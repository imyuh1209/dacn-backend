package vn.bxh.jobhunter.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.Company;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.response.ResCompanyDTO;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.repository.CompanyRepository;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserService userService;

    public Company HandleSaveCompany(Company company){
        return this.companyRepository.save(company);
    }

    public Company FetchCompany(long id){
        Optional<Company> companyOptional = this.companyRepository.findById(id);
        if(companyOptional.isPresent()){
            return companyOptional.get();
        }else{
            throw new IdInvalidException("Id dose not exist!");
        }
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
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(page.getNumber()+1,page.getSize(), page.getTotalPages(), page.getTotalElements());
        resultPaginationDTO.setMeta(meta);
        List<ResCompanyDTO> resList = new ArrayList<>();
        List<Company> companyList = page.getContent();
        for (Company company : companyList) {
            ResCompanyDTO res = new ResCompanyDTO();
            res.setId(company.getId());
            res.setName(company.getName());
            res.setDescription(company.getDescription());
            res.setAddress(company.getAddress());
            resList.add(res);
        }
        resultPaginationDTO.setResult(resList);
        return resultPaginationDTO;
    }



    public void deleteById(Long id){
        this.companyRepository.deleteById(id);
    }

    public Optional<Company> FindById(long id){
        return this.companyRepository.findById(id);
    }
}
