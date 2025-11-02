package iuh.fit.haitebooks_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class HaiTeBooksBackendApplication {

    public static void main(String[] args) {
        // ✅ Nạp biến môi trường từ file .env
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

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
