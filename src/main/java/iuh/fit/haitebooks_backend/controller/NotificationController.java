package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.request.NotificationRequest;
import iuh.fit.haitebooks_backend.dtos.response.NotificationResponse;
import iuh.fit.haitebooks_backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Gửi thông báo
    @PostMapping("/send/{senderId}")
    public ResponseEntity<NotificationResponse> sendNotification(
            @RequestBody NotificationRequest req,
            @PathVariable Long senderId
    ) {
        return ResponseEntity.ok(notificationService.send(req, senderId));
    }

    // Lấy tất cả thông báo user
    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    // Lấy chưa đọc
    @GetMapping("/unread/{userId}")
    public ResponseEntity<List<NotificationResponse>> getUnread(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnread(userId));
    }

    // Đánh dấu đọc
    @PutMapping("/mark-read/{id}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // Đánh dấu tất cả đọc
    @PutMapping("/mark-all-read/{userId}")
    public ResponseEntity<Void> markAllRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    // Xóa 1 thông báo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Xóa tất cả thông báo user
    @DeleteMapping("/all/{userId}")
    public ResponseEntity<Void> deleteAll(@PathVariable Long userId) {
        notificationService.deleteAll(userId);
        return ResponseEntity.noContent().build();
    }
}

