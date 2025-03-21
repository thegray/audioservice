package org.example.audioservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class DatabaseConfig {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void logDbConnection() {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("debug|connected to DB=" + connection.getMetaData().getURL());
        } catch (SQLException e) {
            System.err.println("error|failed to connect to DB=" + e.getMessage());
        }
    }
}
