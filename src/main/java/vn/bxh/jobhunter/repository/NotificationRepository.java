package vn.bxh.jobhunter.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.bxh.jobhunter.domain.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    Page<Notification> findByUser_Email(String email, Pageable pageable);
    long countByUser_EmailAndIsReadFalse(String email);

    // Query để lấy danh sách thông báo đại diện cho Admin (nhóm theo nội dung và thời gian)
    @Query("SELECT n FROM Notification n WHERE n.id IN " +
           "(SELECT MIN(n2.id) FROM Notification n2 GROUP BY n2.createdAt, n2.title, n2.message)")
    Page<Notification> findDistinctNotificationsForAdmin(Pageable pageable);
    
    // Xóa tất cả thông báo có cùng nội dung và thời gian (xử lý xóa hàng loạt)
    @Modifying
    @Transactional
    void deleteByCreatedAtAndTitleAndMessage(java.time.Instant createdAt, String title, String message);
}
