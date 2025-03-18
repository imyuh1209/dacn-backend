package vn.bxh.jobhunter.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.Subscriber;
import vn.bxh.jobhunter.repository.SubscriberRepository;
import vn.bxh.jobhunter.service.SubscriberService;

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


}
