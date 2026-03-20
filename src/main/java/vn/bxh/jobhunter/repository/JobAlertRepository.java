package vn.bxh.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.bxh.jobhunter.domain.JobAlert;

import java.util.List;

@Repository
public interface JobAlertRepository extends JpaRepository<JobAlert, Long> {
    List<JobAlert> findAllByEnabledTrue();
    List<JobAlert> findByEmail(String email);
    List<JobAlert> findByEmailIgnoreCase(String email);
    @Query("select a from JobAlert a where lower(trim(a.email)) = lower(trim(:email))")
    List<JobAlert> findByEmailNormalized(@Param("email") String email);
}
