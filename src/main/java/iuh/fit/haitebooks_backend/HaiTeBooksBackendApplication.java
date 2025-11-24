package iuh.fit.haitebooks_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HaiTeBooksBackendApplication {

    private static final Logger log = LoggerFactory.getLogger(HaiTeBooksBackendApplication.class);

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
            log.info("⚠️ No .env file found, using system environment variables");
        }

        SpringApplication.run(HaiTeBooksBackendApplication.class, args);
    }

}