package vn.bxh.jobhunter.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.Subscriber;
import vn.bxh.jobhunter.repository.SubscriberRepository;
import vn.bxh.jobhunter.service.SubscriberService;
import vn.bxh.jobhunter.util.SecurityUtil;
import vn.bxh.jobhunter.util.anotation.ApiMessage;
import vn.bxh.jobhunter.util.error.IdInvalidException;

@RequestMapping("/api/v1")
@RestController
@AllArgsConstructor
public class SubscriberController {
    private SubscriberService subscriberService;

    @PostMapping("/subscribers")
    public ResponseEntity<Subscriber> AddSub(@RequestBody Subscriber subscriber){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.subscriberService.CreateSubscriber(subscriber));
    }

    @PutMapping("/subscribers")
    public ResponseEntity<Subscriber> UpdateSub(@RequestBody Subscriber subscriber){
        return ResponseEntity.ok(this.subscriberService.UpdateSubscriber(subscriber));
    }

    @GetMapping("/subscribers/skills")
    @ApiMessage("Get subscriber's skill")
    public ResponseEntity<Subscriber> getSubscribersSkill() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        return ResponseEntity.ok().body(this.subscriberService.findByEmail(email));
    }




}
