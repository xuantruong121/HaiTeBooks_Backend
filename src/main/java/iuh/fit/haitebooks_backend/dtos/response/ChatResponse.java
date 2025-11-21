package iuh.fit.haitebooks_backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String response; // Câu trả lời từ AI chatbot
    
    /**
     * Danh sách sách được AI đề xuất trong câu trả lời.
     * Được trích xuất từ response text hoặc fallback về sources nếu không tìm thấy.
     * FE có thể hiển thị dạng buttons để user click xem chi tiết sách.
     */
    private List<String> suggestedBooks;
    
    /**
     * Danh sách sách được tham khảo để tạo context cho AI (RAG - Retrieval-Augmented Generation).
     * Đây là những sách liên quan đến câu hỏi của user, được dùng làm context để AI trả lời.
     * FE có thể hiển thị "Dựa trên thông tin từ: [sources]" để tăng độ tin cậy.
     */
    private List<String> sources;
    
    /**
     * ID cuộc hội thoại. Lần đầu gửi message sẽ tạo mới, các lần sau dùng cùng ID để duy trì context.
     */
    private String conversationId;
}

