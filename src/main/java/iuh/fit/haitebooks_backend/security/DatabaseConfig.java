package iuh.fit.haitebooks_backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource(Environment env) throws URISyntaxException {
        String jdbcUrl = null;
        String username = env.getProperty("DATABASE_USERNAME", env.getProperty("MYSQL_USER"));
        String password = env.getProperty("DATABASE_PASSWORD", env.getProperty("MYSQL_PASSWORD"));

        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            // databaseUrl from Railway: mysql://user:pass@host:port/dbname
            URI dbUri = new URI(databaseUrl);
            String userInfo = dbUri.getUserInfo(); // user:pass
            if (userInfo != null) {
                String[] parts = userInfo.split(":", 2);
                username = parts[0];
                if (parts.length > 1) password = parts[1];
            }
            String host = dbUri.getHost();
            int port = dbUri.getPort();
            String path = dbUri.getPath(); // /dbname
            String dbName = path != null && path.startsWith("/") ? path.substring(1) : path;

            jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    host, port, dbName);
        }

        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        if (jdbcUrl != null) ds.setUrl(jdbcUrl);
        if (username != null) ds.setUsername(username);
        if (password != null) ds.setPassword(password);
        return ds;
    }
}
