package iuh.fit.haitebooks_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class HaiTeBooksBackendApplication {

    public static void main(String[] args) {
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
