package vn.bxh.jobhunter.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.Job;
import vn.bxh.jobhunter.domain.Resume;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.domain.response.Resume.ResResumeDTO;
import vn.bxh.jobhunter.repository.JobRepository;
import vn.bxh.jobhunter.repository.ResumeRepository;
import vn.bxh.jobhunter.repository.UserRepository;
import vn.bxh.jobhunter.util.SecurityUtil;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public long countResumesByJob(Long jobId) {
        return resumeRepository.countByJobId(jobId);
    }

    public ResultPaginationDTO GetAllUser(Specification<Resume> spec, Pageable pageable){
        Page<Resume> pageResume = this.resumeRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageResume.getNumber()+1, pageResume.getSize(), pageResume.getTotalPages(), pageResume.getTotalElements());
        resultPaginationDTO.setMeta(meta);
        List<ResResumeDTO> resResumeDTOS = new ArrayList<>();
        for (Resume resume : pageResume.getContent()){
            resResumeDTOS.add(this.ConvertToResResumeDTO(resume));
        }
        resultPaginationDTO.setResult(resResumeDTOS);
        return resultPaginationDTO;
    }

    public ResultPaginationDTO GetAllByUser(Pageable pageable){
        String email = SecurityUtil.getCurrentUserLogin().isPresent()?SecurityUtil.getCurrentUserLogin().get():"";
        User user = this.userRepository.findByEmail(email);
        Page<Resume> pageResume = this.resumeRepository.findAll(pageable);
        List<Resume> listResumeOfUser =new ArrayList<>();
        for(Resume re : pageResume.getContent()){
            if(re.getUser()!=null){
                if(re.getUser().equals(user)){
                    listResumeOfUser.add(re);
                }
            }
        }
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageResume.getNumber()+1, pageResume.getSize(), pageResume.getTotalPages(), pageResume.getTotalElements());
        resultPaginationDTO.setMeta(meta);
        List<ResResumeDTO> resResumeDTOS = new ArrayList<>();
        for (Resume resume : listResumeOfUser){
            resResumeDTOS.add(this.ConvertToResResumeDTO(resume));
        }
        resultPaginationDTO.setResult(resResumeDTOS);
        return resultPaginationDTO;
    }

    public ResultPaginationDTO GetAllResume(Specification<Resume> spec, Pageable pageable){
        Page<Resume> pageResume = this.resumeRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageResume.getNumber()+1, pageResume.getSize(), pageResume.getTotalPages(), pageResume.getTotalElements());
        resultPaginationDTO.setMeta(meta);
        List<ResResumeDTO> resResumeDTOS = new ArrayList<>();
        for (Resume resume : pageResume.getContent()){
            resResumeDTOS.add(this.ConvertToResResumeDTO(resume));
        }
        resultPaginationDTO.setResult(resResumeDTOS);
        return resultPaginationDTO;
    }

    public ResResumeDTO GetResume(long id){
        Optional<Resume> resumeOptional = this.resumeRepository.findById(id);
        if(resumeOptional.isPresent()){
            return ConvertToResResumeDTO(resumeOptional.get());
        }else{
            throw new IdInvalidException("Id is not valid!");
        }
    }

    public void DeleteResume(long id){
        Optional<Resume> resumeOptional = this.resumeRepository.findById(id);
        if(resumeOptional.isPresent()){
            this.resumeRepository.deleteById(id);
        }else{
            throw new IdInvalidException("Id is not valid!");
        }
    }

    public Resume HandleUpdateResume(Resume resume){
        Optional<Resume> resumeOptional = this.resumeRepository.findById(resume.getId());
        if(resumeOptional.isPresent()){
            Resume resumeUpdate = resumeOptional.get();
            if(resume.getUser()!=null){
                resumeUpdate.setUser(resume.getUser());
            }
            resumeUpdate.setStatus(resume.getStatus());
            return this.resumeRepository.save(resumeUpdate);
        }else{
            throw new IdInvalidException("Id is not valid!");
        }
    }

    public Resume HandleCreateResume(Resume resume){
        if(resume.getUser()==null || resume.getJob()==null){
            throw new IdInvalidException("User or job must not be left empty.");
        }
        Optional<Job> job = this.jobRepository.findById(resume.getJob().getId());
        Optional<User> user = this.userRepository.findById(resume.getUser().getId());
        resume.setJob(job.orElse(null));
        resume.setUser(user.orElse(null));
        return this.resumeRepository.save(resume);
    }

    public ResResumeDTO ConvertToResResumeDTO(Resume resume){
        ResResumeDTO res = new ResResumeDTO();
        res.setId(resume.getId());
        res.setStatus(resume.getStatus());
        res.setEmail(resume.getEmail());
        res.setUrl(resume.getUrl());
        res.setCreatedBy(resume.getCreatedBy());
        res.setUpdatedAt(resume.getUpdatedAt());
        res.setCreatedAt(resume.getCreatedAt());
        res.setUpdatedBy(resume.getUpdatedBy());
        if(resume.getUser()!=null){
            ResResumeDTO.User user = new ResResumeDTO.User(resume.getUser().getId(), resume.getUser().getName());
            res.setUser(user);
        }
        if(resume.getJob()!= null){
            ResResumeDTO.Job job = new ResResumeDTO.Job(resume.getJob().getId(),resume.getJob().getName());
            res.setJob(job);
        }
        return res;
    }

    public vn.bxh.jobhunter.domain.response.ResEmailStatusDTO SendResumeStatusEmail(
            vn.bxh.jobhunter.domain.request.ReqResumeStatusEmail req) {
        Long resumeId = req.getResumeId();
        String rawStatus = req.getStatus();

        if (rawStatus == null || rawStatus.trim().isEmpty()) {
            throw new IdInvalidException("Status is required");
        }

        String normalized = rawStatus.trim().toUpperCase();
        vn.bxh.jobhunter.util.Constant.ResumeStateEnum state;
        try {
            state = vn.bxh.jobhunter.util.Constant.ResumeStateEnum.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IdInvalidException("Invalid status: " + rawStatus);
        }

        Resume resume = this.resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IdInvalidException("Id is not valid!"));

        String toEmail = resume.getEmail();
        if (toEmail == null || toEmail.isBlank()) {
            // fallback to user email if available
            if (resume.getUser() != null) {
                toEmail = resume.getUser().getEmail();
            }
        }

        vn.bxh.jobhunter.domain.response.ResEmailStatusDTO res = new vn.bxh.jobhunter.domain.response.ResEmailStatusDTO();
        if (toEmail == null || toEmail.isBlank()) {
            res.setSent(false);
            res.setSkipped(true);
            return res;
        }

        String companyName = (resume.getJob() != null && resume.getJob().getCompany() != null)
                ? resume.getJob().getCompany().getName() : "Công ty";
        String subject = switch (state) {
            case PENDING -> companyName + " – Xác nhận đã nhận được hồ sơ ứng tuyển của bạn";
            case REVIEWING -> companyName + " – Hồ sơ của bạn đang được xem xét";
            case APPROVED -> companyName + " – Chúc mừng! Bạn đã vượt qua vòng tuyển chọn hồ sơ";
            case REJECTED -> companyName + " – Kết quả hồ sơ ứng tuyển của bạn";
        };

        String userName = resume.getUser() != null ? resume.getUser().getName() : toEmail;
        String jobName = resume.getJob() != null ? resume.getJob().getName() : "Vị trí";

        this.emailService.sendResumeStatusEmail(
                toEmail,
                subject,
                userName,
                jobName,
                companyName,
                state.name());
        res.setSent(true);
        res.setSkipped(false);
        return res;
    }

}
