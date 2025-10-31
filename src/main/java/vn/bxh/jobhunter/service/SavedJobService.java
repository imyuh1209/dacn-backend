package vn.bxh.jobhunter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bxh.jobhunter.domain.Job;
import vn.bxh.jobhunter.domain.SavedJob;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.repository.JobRepository;
import vn.bxh.jobhunter.repository.SavedJobRepository;
import vn.bxh.jobhunter.repository.UserRepository;
import vn.bxh.jobhunter.util.SecurityUtil;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.List;
// import java.util.Objects; // nếu getId() là Long và bạn muốn dùng Objects.equals

@Service
@RequiredArgsConstructor
public class SavedJobService {

    private final SavedJobRepository savedJobRepo;
    private final UserRepository userRepo;
    private final JobRepository jobRepo;

    private User getCurrentUserOrThrow() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Not authenticated"));

        User user = userRepo.findByEmail(username);
        if (user == null) {
            throw new IdInvalidException("User not found");
        }
        return user;
    }

    @Transactional
    public SavedJob saveJob(Long jobId) {
        User user = getCurrentUserOrThrow();

        // 1) Kiểm tra job có tồn tại không
        Job job = jobRepo.findById(jobId)
                .orElseThrow(() -> new IdInvalidException("Job not found: " + jobId));

        // 2) Nếu đã lưu rồi -> trả luôn bản hiện có (idempotent)
        return savedJobRepo.findByUserIdAndJobId(user.getId(), jobId)
                .orElseGet(() -> {
                    SavedJob sj = new SavedJob();
                    sj.setUser(user);
                    sj.setJob(job);
                    return savedJobRepo.save(sj);
                });
    }

    @Transactional(readOnly = true)
    public List<SavedJob> listMySavedJobs() {
        User user = getCurrentUserOrThrow();
        return savedJobRepo.findAllByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public void removeSavedJob(Long savedJobIdOrJobId, boolean byJobId) {
        User user = getCurrentUserOrThrow();

        if (byJobId) {
            // Xóa theo jobId
            SavedJob sj = savedJobRepo.findByUserIdAndJobId(user.getId(), savedJobIdOrJobId)
                    .orElseThrow(() -> new IdInvalidException("Saved job not found"));
            savedJobRepo.delete(sj);
        } else {
            // Xóa theo savedJobId
            SavedJob sj = savedJobRepo.findById(savedJobIdOrJobId)
                    .orElseThrow(() -> new IdInvalidException("Saved job not found"));

            // Nếu getId() trả về kiểu nguyên thủy long:
            if (sj.getUser().getId() != user.getId()) {
                throw new IdInvalidException("Forbidden");
            }

            // Nếu getId() là Long, có thể dùng:
            // if (!Objects.equals(sj.getUser().getId(), user.getId())) { ... }

            savedJobRepo.delete(sj);
        }
    }

    @Transactional(readOnly = true)
    public boolean isSaved(Long jobId) {
        User user = getCurrentUserOrThrow();
        return savedJobRepo.findByUserIdAndJobId(user.getId(), jobId).isPresent();
    }
}
