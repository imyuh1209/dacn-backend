package vn.bxh.jobhunter.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.Notification;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.request.ReqNotificationDTO;
import vn.bxh.jobhunter.domain.response.ResNotificationDTO;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.repository.NotificationRepository;
import vn.bxh.jobhunter.repository.UserRepository;
import vn.bxh.jobhunter.util.SecurityUtil;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public ResultPaginationDTO fetchAllNotifications(Specification<Notification> spec, Pageable pageable) {
        // Sử dụng query custom để chỉ lấy các thông báo đại diện (tránh spam hiển thị)
        Page<Notification> page = this.notificationRepository.findDistinctNotificationsForAdmin(pageable);
        
        List<ResNotificationDTO> list = page.getContent().stream().map(n -> new ResNotificationDTO(
                n.getId(), n.getTitle(), n.getMessage(), n.getSenderName(), n.isRead(), n.getCreatedAt()
        )).collect(Collectors.toList());

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                page.getNumber() + 1, page.getSize(), page.getTotalPages(), page.getTotalElements()
        );
        rs.setMeta(meta);
        rs.setResult(list);
        return rs;
    }

    @Transactional
    public void deleteNotification(long id) {
        Optional<Notification> noti = this.notificationRepository.findById(id);
        if (noti.isPresent()) {
            Notification n = noti.get();
            // Xóa tất cả các thông báo có cùng nội dung và thời gian tạo (batch delete)
            this.notificationRepository.deleteByCreatedAtAndTitleAndMessage(n.getCreatedAt(), n.getTitle(), n.getMessage());
        }
    }

    @Async
    public void createNotification(ReqNotificationDTO req) {
        // Lấy danh sách tất cả user
        List<User> allUsers = this.userRepository.findAll();
        
        // Determine sender name based on current logged in user
        String senderEmail = SecurityUtil.getCurrentUserLogin().orElse("System");
        User sender = this.userRepository.findByEmail(senderEmail);
        String senderName = "System";
        if (sender != null) {
            if (sender.getCompany() != null) {
                senderName = sender.getCompany().getName();
            } else {
                senderName = "Admin";
            }
        }

        List<Notification> notificationsToSave = new ArrayList<>();
        java.time.Instant now = java.time.Instant.now(); // Đồng bộ thời gian tạo cho cả batch
        for (User user : allUsers) {
            Notification notification = new Notification();
            notification.setTitle(req.getTitle());
            notification.setMessage(req.getMessage());
            notification.setUser(user);
            notification.setSenderName(senderName);
            notification.setCreatedAt(now); // Gán thời gian tạo thủ công
            notificationsToSave.add(notification);
        }

        if (!notificationsToSave.isEmpty()) {
            this.notificationRepository.saveAll(notificationsToSave);
        }
    }

    public ResultPaginationDTO getMyNotifications(Pageable pageable) {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        Page<Notification> page = this.notificationRepository.findByUser_Email(email, pageable);
        
        List<ResNotificationDTO> list = page.getContent().stream().map(n -> new ResNotificationDTO(
                n.getId(), n.getTitle(), n.getMessage(), n.getSenderName(), n.isRead(), n.getCreatedAt()
        )).collect(Collectors.toList());

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                page.getNumber() + 1, page.getSize(), page.getTotalPages(), page.getTotalElements()
        );
        rs.setMeta(meta);
        rs.setResult(list);
        return rs;
    }

    public void markAsRead(long id) {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        Optional<Notification> noti = this.notificationRepository.findById(id);
        if (noti.isPresent()) {
            Notification n = noti.get();
            // Ensure the notification belongs to the current user
            if (n.getUser().getEmail().equals(email)) {
                n.setRead(true);
                this.notificationRepository.save(n);
            }
        }
    }
    
    public long countUnread() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        return this.notificationRepository.countByUser_EmailAndIsReadFalse(email);
    }
}
