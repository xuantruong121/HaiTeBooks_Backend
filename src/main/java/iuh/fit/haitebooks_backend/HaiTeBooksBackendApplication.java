package iuh.fit.haitebooks_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class HaiTeBooksBackendApplication {

    public static void main(String[] args) {
        // ✅ Nạp biến môi trường từ file .env (optional - fallback về env vars)
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()  // ✅ Không throw exception nếu không có file .env
                    .load();
            dotenv.entries().forEach(entry ->
                    System.setProperty(entry.getKey(), entry.getValue())
            );
        } catch (Exception e) {
            // Nếu không có file .env, sử dụng environment variables từ hệ thống
            System.out.println("⚠️ No .env file found, using system environment variables");
        }

        SpringApplication.run(HaiTeBooksBackendApplication.class, args);

//        // Tạo instance của BCrypt encoder
//        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//
//        // Mã hóa mật khẩu
//        String rawPassword = "user123";
//        String encodedPassword = passwordEncoder.encode(rawPassword);
//
//        // In ra console
//        System.out.println("BCrypt encoded password: " + encodedPassword);
    }

}