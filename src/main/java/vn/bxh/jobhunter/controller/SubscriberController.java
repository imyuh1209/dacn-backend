package vn.bxh.jobhunter.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import vn.bxh.jobhunter.domain.Subscriber;
import vn.bxh.jobhunter.domain.response.ResSubscriberDTO;
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

    @GetMapping("/subscribers")
    @ApiMessage("Get all subscribers")
    public ResponseEntity<List<ResSubscriberDTO>> GetAllSubscribers() {
        List<Subscriber> list = this.subscriberService.GetAllSubscribers();
        List<ResSubscriberDTO> res = list.stream().map(s -> {
            ResSubscriberDTO dto = new ResSubscriberDTO();
            dto.setId(s.getId());
            dto.setEmail(s.getEmail());
            dto.setName(s.getName());
            dto.setCreatedAt(s.getCreatedAt());
            dto.setUpdatedAt(s.getUpdatedAt());
            return dto;
        }).toList();
        return ResponseEntity.ok().body(res);
    }

    @GetMapping("/subscribers/me")
    @ApiMessage("Get current user's subscriber")
    public ResponseEntity<ResSubscriberDTO> GetMySubscriber() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Không xác định được người dùng hiện tại"));

        Subscriber s = this.subscriberService.findByEmail(email);
        if (s == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        ResSubscriberDTO dto = new ResSubscriberDTO();
        dto.setId(s.getId());
        dto.setEmail(s.getEmail());
        dto.setName(s.getName());
        dto.setCreatedAt(s.getCreatedAt());
        dto.setUpdatedAt(s.getUpdatedAt());
        return ResponseEntity.ok().body(dto);
    }

    // Removed skills endpoint per request




}
