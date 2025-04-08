package vn.bxh.jobhunter.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.Job;
import vn.bxh.jobhunter.domain.Skill;
import vn.bxh.jobhunter.domain.Subscriber;
import vn.bxh.jobhunter.domain.response.Email.ResEmailJob;
import vn.bxh.jobhunter.repository.JobRepository;
import vn.bxh.jobhunter.repository.SkillRepository;
import vn.bxh.jobhunter.repository.SubscriberRepository;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SubscriberService {
    private final SubscriberRepository subscriberRepository;
    private final SkillRepository skillRepository;
    private final EmailService emailService;
    private final JobRepository jobRepository;

    public Subscriber CreateSubscriber(Subscriber subscriber){
        Optional<Subscriber> subOptional = this.subscriberRepository.findByEmail(subscriber.getEmail());
        if(subOptional.isEmpty()){
            if(subscriber.getSkills()!=null){
                List<Skill> skills = new ArrayList<>();
                for (Skill skill : subscriber.getSkills()){
                    Optional<Skill> skillOptional = this.skillRepository.findById(skill.getId());
                    skillOptional.ifPresent(skills::add);
                }
                subscriber.setSkills(skills);
            }
            return this.subscriberRepository.save(subscriber);
        }throw new IdInvalidException("Name is not valid!");
    }

    public Subscriber UpdateSubscriber(Subscriber subscriber){
        Optional<Subscriber> subscriberOptional = this.subscriberRepository.findById(subscriber.getId());
        if(subscriberOptional.isPresent()){
            Subscriber subDB = subscriberOptional.get();
            if(subscriber.getSkills()!=null){
                List<Skill> skills = new ArrayList<>();
                for (Skill skill : subscriber.getSkills()){
                    Optional<Skill> skillOptional = this.skillRepository.findById(skill.getId());
                    skillOptional.ifPresent(skills::add);
                }
                subDB.setSkills(skills);
            }
            return this.subscriberRepository.save(subDB);
        }throw new IdInvalidException("Id does not exist!");
    }

    public void sendSubscribersEmailJobs() {
        List<Subscriber> listSubs = this.subscriberRepository.findAll();
        if (listSubs != null && listSubs.size() > 0) {
            for (Subscriber sub : listSubs) {
                List<Skill> listSkills = sub.getSkills();
                if (listSkills != null && listSkills.size() > 0) {
                    List<Job> listJobs = this.jobRepository.findBySkillsIn(listSkills);
                    if (listJobs != null && listJobs.size() > 0) {
                        List<ResEmailJob> arr = listJobs.stream().map(
                                job -> this.convertJobToSendEmail(job)).collect(Collectors.toList());
                        this.emailService.sendEmailFromTemplateSync(
                                sub.getEmail(),
                                "Cơ hội việc làm hot đang chờ đón bạn, khám phá ngay",
                                "job",
                                sub.getEmail(),
                                arr);
                    }
                }
            }
        }
    }

    public ResEmailJob convertJobToSendEmail(Job job) {
        ResEmailJob res = new ResEmailJob();
        res.setName(job.getName());
        res.setSalary(job.getSalary());
        res.setCompany(new ResEmailJob.CompanyEmail(job.getCompany().getName()));
        List<Skill> skills = job.getSkills();
        List<ResEmailJob.SkillEmail> s = skills.stream().map(skill -> new
                        ResEmailJob.SkillEmail(skill.getName()))
                .collect(Collectors.toList());
        res.setSkills(s);
        return res;
    }

    public Subscriber findByEmail(String email) {
        return this.subscriberRepository.findByEmail(email).get();
    }
}
