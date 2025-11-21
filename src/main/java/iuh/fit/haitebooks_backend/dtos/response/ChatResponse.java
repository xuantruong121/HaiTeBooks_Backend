package iuh.fit.haitebooks_backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String response;
    private List<String> suggestedBooks; // Danh sách sách được đề xuất (nếu có)
    private List<String> sources; // Nguồn thông tin (tên sách được tham khảo)
    private String conversationId;
}

