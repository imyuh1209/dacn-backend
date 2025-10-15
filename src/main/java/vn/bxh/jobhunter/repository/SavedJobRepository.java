package vn.bxh.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.bxh.jobhunter.domain.SavedJob;
import java.util.List;
import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    boolean existsByUserIdAndJobId(Long userId, Long jobId);
    Optional<SavedJob> findByUserIdAndJobId(Long userId, Long jobId);
    List<SavedJob> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
