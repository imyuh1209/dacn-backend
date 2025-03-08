package vn.bxh.jobhunter.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.Company;
import vn.bxh.jobhunter.domain.dto.Meta;
import vn.bxh.jobhunter.domain.dto.ResultPaginationDTO;
import vn.bxh.jobhunter.service.CompanyService;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("")
@AllArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping("/companies")
    public ResponseEntity<Company> createNewUser(@Valid @RequestBody Company company) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.companyService.HandleSaveCompany(company));
    }

    @GetMapping("/companies")
    public ResponseEntity<ResultPaginationDTO> getListCompanies(@Filter Specification<Company> spec, Pageable pageable){

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(this.companyService.FetchAllCompanies(spec, pageable));
    }

    @PutMapping("/companies")
    public ResponseEntity<Company> getListCompanies(@Valid @RequestBody Company company){
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(this.companyService.HandleUpdateCompany(company));
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Void> getListCompanies(@PathVariable Long id){
        this.companyService.deleteById(id);
         return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
