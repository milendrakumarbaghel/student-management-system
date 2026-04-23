package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {

    private static final String URL = System.getenv().getOrDefault(
            "DB_URL",
            "jdbc:mysql://localhost:3306/student_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    );
    private static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "admin");

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        String createStudentTable = "CREATE TABLE IF NOT EXISTS student ("
                + "id INT PRIMARY KEY,"
                + "name VARCHAR(50) NOT NULL,"
                + "age INT NOT NULL,"
                + "branch VARCHAR(50) NOT NULL"
                + ")";

        String createRegistrationTable = "CREATE TABLE IF NOT EXISTS registration ("
                + "reg_id INT PRIMARY KEY AUTO_INCREMENT,"
                + "student_id INT NOT NULL,"
                + "course_name VARCHAR(50) NOT NULL,"
                + "fees_paid DOUBLE NOT NULL,"
                + "FOREIGN KEY (student_id) REFERENCES student(id)"
                + ")";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createStudentTable);
            statement.execute(createRegistrationTable);

            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.out.println("Database initialization failed: " + e.getMessage());
        }
    }
}

