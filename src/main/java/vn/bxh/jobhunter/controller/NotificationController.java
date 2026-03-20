package vn.bxh.jobhunter.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.jpa.domain.Specification;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.Notification;
import vn.bxh.jobhunter.domain.request.ReqNotificationDTO;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.service.NotificationService;
import vn.bxh.jobhunter.util.anotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/notifications")
    @ApiMessage("Create a notification")
    public ResponseEntity<Void> createNotification(@Valid @RequestBody ReqNotificationDTO req) {
        this.notificationService.createNotification(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/notifications")
    @ApiMessage("Get my notifications")
    public ResponseEntity<ResultPaginationDTO> getMyNotifications(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(this.notificationService.getMyNotifications(pageable));
    }

    @GetMapping("/notifications/admin")
    @ApiMessage("Get all notifications (Admin)")
    public ResponseEntity<ResultPaginationDTO> getAllNotifications(
            @Filter Specification<Notification> spec,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        // Fix: Frontend sends sort=updatedAt by default, but entity only has createdAt
        // We map updatedAt -> createdAt manually to avoid PropertyReferenceException
        Sort sort = pageable.getSort();
        if (sort.getOrderFor("updatedAt") != null) {
            Sort newSort = Sort.by(Sort.Direction.DESC, "createdAt");
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(), 
                    pageable.getPageSize(), 
                    newSort);
        }
        
        return ResponseEntity.ok(this.notificationService.fetchAllNotifications(spec, pageable));
    }

    @PutMapping("/notifications/{id}/read")
    @ApiMessage("Mark notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable long id) {
        this.notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/notifications/count-unread")
    @ApiMessage("Count unread notifications")
    public ResponseEntity<Long> countUnread() {
        return ResponseEntity.ok(this.notificationService.countUnread());
    }

    @DeleteMapping("/notifications/{id}")
    @ApiMessage("Delete notification")
    public ResponseEntity<Void> deleteNotification(@PathVariable long id) {
        this.notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }
}
