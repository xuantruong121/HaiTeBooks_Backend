package iuh.fit.haitebooks_backend.dtos.request;

import lombok.Data;

@Data
public class NotificationRequest {
    private Long receiverId;  // user nhận
    private String title;
    private String content;
}
