package iuh.fit.haitebooks_backend.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    @NotBlank(message = "Message không được để trống")
    private String message;
    
    // Optional: Conversation history để context
    private String conversationId;
}

