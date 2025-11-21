package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.service.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);
    private final CloudinaryService cloudinaryService;

    @Autowired
    public UploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Upload ảnh lên Cloudinary
     * POST /api/upload/image
     * Content-Type: multipart/form-data
     * Body: file (MultipartFile), folder (optional, default: "books")
     * 
     * Response:
     * {
     *   "success": true,
     *   "imageUrl": "https://res.cloudinary.com/.../books/abc123.jpg",
     *   "message": "Upload ảnh thành công"
     * }
     */
    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "books") String folder
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("📤 Nhận yêu cầu upload ảnh: {} (folder: {})", 
                    file.getOriginalFilename(), folder);

            // Upload lên Cloudinary
            String imageUrl = cloudinaryService.uploadImage(file, folder);
            
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("message", "Upload ảnh thành công");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // Lỗi validate (file rỗng, không phải ảnh, quá lớn)
            log.warn("⚠️ Lỗi validate file: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            // Lỗi khác (network, Cloudinary API, ...)
            log.error("❌ Lỗi khi upload ảnh: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Lỗi khi upload ảnh: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

