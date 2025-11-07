package vn.bxh.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.bxh.jobhunter.domain.Resume;

import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume,Long>, JpaSpecificationExecutor<Resume> {

    boolean existsByEmail(String email);
    Optional<Resume> findByEmail(String email);

    long countByJobId(Long jobId);
    boolean existsByUser_IdAndJob_Id(Long userId, Long jobId);
    org.springframework.data.domain.Page<Resume> findAllByUser_Id(Long userId, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Resume> findAllByUser_IdAndJobIsNull(Long userId, org.springframework.data.domain.Pageable pageable);
}
