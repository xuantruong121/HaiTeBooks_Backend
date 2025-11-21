package iuh.fit.haitebooks_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload ảnh lên Cloudinary
     * @param file File ảnh từ client
     * @param folder Thư mục lưu trên Cloudinary (ví dụ: "books", "users")
     * @return URL của ảnh sau khi upload
     * @throws IOException Nếu có lỗi khi upload
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File phải là ảnh (jpg, png, gif, webp)");
        }

        // Validate file size (max 5MB)
        long fileSize = file.getSize();
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File không được vượt quá 5MB");
        }

        log.info("📤 Bắt đầu upload ảnh: {} (size: {} bytes, type: {})", 
                file.getOriginalFilename(), fileSize, contentType);

        // Cấu hình upload
        // ✅ Tạm thời bỏ transformation để test upload cơ bản
        // Transformation sẽ được thêm lại sau khi upload hoạt động
        Map<String, Object> uploadParams = ObjectUtils.asMap(
            "folder", folder, // Lưu vào folder trên Cloudinary
            "resource_type", "image",
            "overwrite", true // Ghi đè nếu file trùng tên
        );

        try {
            // Upload file
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                uploadParams
            );

            // Lấy URL từ kết quả
            String imageUrl = (String) uploadResult.get("secure_url"); // Dùng secure_url (HTTPS)
            
            log.info("✅ Upload ảnh thành công: {}", imageUrl);
            return imageUrl;

        } catch (IOException e) {
            log.error("❌ Lỗi khi upload ảnh lên Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Lỗi khi upload ảnh: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa ảnh khỏi Cloudinary (khi xóa sách hoặc cập nhật ảnh)
     * @param imageUrl URL của ảnh cần xóa
     * @throws IOException Nếu có lỗi khi xóa
     */
    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        // Extract public_id từ URL
        try {
            String publicId = extractPublicId(imageUrl);
            if (publicId == null) {
                log.warn("⚠️ Không thể extract public_id từ URL: {}", imageUrl);
                return;
            }

            log.info("🗑️ Xóa ảnh từ Cloudinary: {}", publicId);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("✅ Đã xóa ảnh thành công: {}", publicId);

        } catch (Exception e) {
            // Log lỗi nhưng không throw để không ảnh hưởng đến flow chính
            log.error("❌ Lỗi khi xóa ảnh từ Cloudinary: {}", e.getMessage(), e);
        }
    }

    /**
     * Extract public_id từ Cloudinary URL
     * URL format: https://res.cloudinary.com/{cloud_name}/image/upload/{version}/{public_id}.{format}
     * Hoặc: https://res.cloudinary.com/{cloud_name}/image/upload/{transformation}/{public_id}.{format}
     */
    private String extractPublicId(String imageUrl) {
        if (!imageUrl.contains("cloudinary.com")) {
            return null; // Không phải Cloudinary URL
        }

        try {
            // Tách URL để lấy phần sau "upload/"
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            String afterUpload = parts[1];
            // Bỏ phần version (v1234567890) nếu có
            String[] segments = afterUpload.split("/");
            String publicIdWithFormat = segments[segments.length - 1];

            // Bỏ extension (.jpg, .png, .webp)
            int lastDot = publicIdWithFormat.lastIndexOf('.');
            if (lastDot > 0) {
                String publicId = publicIdWithFormat.substring(0, lastDot);
                // Nếu có folder, giữ lại folder
                if (segments.length > 1) {
                    String folder = segments[segments.length - 2];
                    return folder + "/" + publicId;
                }
                return publicId;
            }

            return publicIdWithFormat;

        } catch (Exception e) {
            log.error("❌ Lỗi khi extract public_id: {}", e.getMessage());
            return null;
        }
    }
}

