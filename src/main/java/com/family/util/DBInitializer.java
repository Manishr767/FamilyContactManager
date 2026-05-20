package com.family.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class DBInitializer {

    public static void initialize() {
        String createDB = "CREATE DATABASE IF NOT EXISTS family_contacts";
        String useDB   = "USE family_contacts";

        String createContacts = """
            CREATE TABLE IF NOT EXISTS contacts (
                id          INT AUTO_INCREMENT PRIMARY KEY,
                first_name  VARCHAR(100) NOT NULL,
                last_name   VARCHAR(100) NOT NULL,
                relationship VARCHAR(100),
                phone       VARCHAR(30),
                email       VARCHAR(150),
                address     VARCHAR(255),
                birthday    DATE,
                notes       TEXT,
                photo_path  VARCHAR(500),
                created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;

        String createGroups = """
            CREATE TABLE IF NOT EXISTS contact_groups (
                id         INT AUTO_INCREMENT PRIMARY KEY,
                group_name VARCHAR(100) NOT NULL UNIQUE
            )
        """;

        String createMapping = """
            CREATE TABLE IF NOT EXISTS contact_group_map (
                contact_id INT,
                group_id   INT,
                PRIMARY KEY (contact_id, group_id),
                FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE,
                FOREIGN KEY (group_id)   REFERENCES contact_groups(id) ON DELETE CASCADE
            )
        """;

        String[] defaultGroups = {
            "INSERT IGNORE INTO contact_groups (group_name) VALUES ('Immediate Family')",
            "INSERT IGNORE INTO contact_groups (group_name) VALUES ('Extended Family')",
            "INSERT IGNORE INTO contact_groups (group_name) VALUES ('Friends')",
            "INSERT IGNORE INTO contact_groups (group_name) VALUES ('Emergency Contacts')"
        };

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                getUser(), getPassword());
             Statement stmt = conn.createStatement()) {

            stmt.execute(createDB);
            stmt.execute(useDB);
            stmt.execute(createContacts);
            stmt.execute(createGroups);
            stmt.execute(createMapping);
            for (String sql : defaultGroups) stmt.execute(sql);

            System.out.println("✅ Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("❌ DB init failed: " + e.getMessage());
            // Will surface in UI as connection error
        }
    }

    private static String getUser()     { return "root"; }
    private static String getPassword() { return "your_password"; } // Change this

    // Allow standalone DriverManager import
    @SuppressWarnings("unused")
    private static java.sql.Connection dummy;
    static {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (Exception ignored) {}
    }
    // Use full DriverManager
    private static java.sql.Connection getConn(String url) throws SQLException {
        return java.sql.DriverManager.getConnection(url, getUser(), getPassword());
    }
}
