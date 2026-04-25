package util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
                + "branch_id INT NULL,"
                + "branch VARCHAR(50) NOT NULL"
                + ")";

        String createBranchTable = "CREATE TABLE IF NOT EXISTS branches ("
                + "branch_id INT PRIMARY KEY AUTO_INCREMENT,"
                + "branch_name VARCHAR(50) NOT NULL UNIQUE"
                + ")";

        String createCoursesTable = "CREATE TABLE IF NOT EXISTS courses ("
                + "course_id INT PRIMARY KEY AUTO_INCREMENT,"
                + "branch_id INT NOT NULL,"
                + "course_name VARCHAR(50) NOT NULL,"
                + "UNIQUE KEY uq_course_branch (branch_id, course_name),"
                + "FOREIGN KEY (branch_id) REFERENCES branches(branch_id)"
                + ")";

        String createRegistrationTable = "CREATE TABLE IF NOT EXISTS registration ("
                + "reg_id INT PRIMARY KEY AUTO_INCREMENT,"
                + "student_id INT NOT NULL,"
                + "course_id INT NULL,"
                + "course_name VARCHAR(50) NOT NULL,"
                + "fees_paid DOUBLE NOT NULL,"
                + "FOREIGN KEY (student_id) REFERENCES student(id),"
                + "FOREIGN KEY (course_id) REFERENCES courses(course_id)"
                + ")";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createStudentTable);
            statement.execute(createBranchTable);
            statement.execute(createCoursesTable);
            statement.execute(createRegistrationTable);
            ensureStudentBranchColumn(connection);
            backfillStudentBranchId(connection);
            ensureStudentBranchForeignKey(connection);
            ensureCoursesBranchColumn(connection);
            ensureCoursesBranchForeignKey(connection);
            ensureRegistrationCourseColumn(connection);
            ensureRegistrationCourseForeignKey(connection);

            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.out.println("Database initialization failed: " + e.getMessage());
        }
    }

    private static void ensureCoursesBranchColumn(Connection connection) throws SQLException {
        if (!columnExists(connection, "courses", "branch_id")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE courses ADD COLUMN branch_id INT NULL AFTER course_id");
            }
        }
    }

    private static void ensureStudentBranchColumn(Connection connection) throws SQLException {
        if (!columnExists(connection, "student", "branch_id")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE student ADD COLUMN branch_id INT NULL AFTER age");
            }
        }
    }

    private static void backfillStudentBranchId(Connection connection) throws SQLException {
        if (!columnExists(connection, "student", "branch") || !columnExists(connection, "student", "branch_id")) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE student s JOIN branches b ON s.branch = b.branch_name SET s.branch_id = b.branch_id WHERE s.branch_id IS NULL");
        }
    }

    private static void ensureStudentBranchForeignKey(Connection connection) throws SQLException {
        if (foreignKeyForColumnExists(connection, "student", "branch_id")) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE student ADD CONSTRAINT fk_student_branch FOREIGN KEY (branch_id) REFERENCES branches(branch_id)");
        }
    }

    private static void ensureCoursesBranchForeignKey(Connection connection) throws SQLException {
        if (foreignKeyForColumnExists(connection, "courses", "branch_id")) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE courses ADD CONSTRAINT fk_courses_branch FOREIGN KEY (branch_id) REFERENCES branches(branch_id)");
        }
    }

    private static void ensureRegistrationCourseColumn(Connection connection) throws SQLException {
        if (!columnExists(connection, "registration", "course_id")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE registration ADD COLUMN course_id INT NULL AFTER student_id");
            }
        }
    }

    private static void ensureRegistrationCourseForeignKey(Connection connection) throws SQLException {
        if (foreignKeyForColumnExists(connection, "registration", "course_id")) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE registration ADD CONSTRAINT fk_registration_course FOREIGN KEY (course_id) REFERENCES courses(course_id)");
        }
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, tableName, columnName)) {
            return resultSet.next();
        }
    }

    private static boolean foreignKeyForColumnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getImportedKeys(connection.getCatalog(), null, tableName)) {
            while (resultSet.next()) {
                if (columnName.equalsIgnoreCase(resultSet.getString("FKCOLUMN_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }
}

