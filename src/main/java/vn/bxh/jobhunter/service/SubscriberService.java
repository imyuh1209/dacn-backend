package vn.bxh.jobhunter.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.Skill;
import vn.bxh.jobhunter.domain.Subscriber;
import vn.bxh.jobhunter.repository.SkillRepository;
import vn.bxh.jobhunter.repository.SubscriberRepository;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SubscriberService {
    private final SubscriberRepository subscriberRepository;
    private final SkillRepository skillRepository;

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
}
