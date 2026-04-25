package dao;

import model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDAO {

    public boolean insertStudent(Connection connection, Student student) throws SQLException {
        String sql = "INSERT INTO student (id, name, age, branch_id, branch) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, student.getId());
            preparedStatement.setString(2, student.getName());
            preparedStatement.setInt(3, student.getAge());
            preparedStatement.setInt(4, student.getBranchId());
            preparedStatement.setString(5, student.getBranch());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean studentExists(Connection connection, int studentId) throws SQLException {
        String sql = "SELECT 1 FROM student WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, studentId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public Student findById(Connection connection, int studentId) throws SQLException {
        String sql = "SELECT s.id, s.name, s.age, s.branch_id, COALESCE(b.branch_name, s.branch) AS branch "
                + "FROM student s "
                + "LEFT JOIN branches b ON s.branch_id = b.branch_id "
                + "WHERE s.id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, studentId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new Student(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("age"),
                        resultSet.getInt("branch_id"),
                        resultSet.getString("branch")
                );
            }
        }
    }

    public boolean updateStudentName(Connection connection, int studentId, String name) throws SQLException {
        String sql = "UPDATE student SET name = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, studentId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean updateStudentAge(Connection connection, int studentId, int age) throws SQLException {
        String sql = "UPDATE student SET age = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, age);
            preparedStatement.setInt(2, studentId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean updateStudentBranch(Connection connection, int studentId, int branchId, String branchName) throws SQLException {
        String sql = "UPDATE student SET branch_id = ?, branch = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, branchId);
            preparedStatement.setString(2, branchName);
            preparedStatement.setInt(3, studentId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean deleteStudent(Connection connection, int studentId) throws SQLException {
        String sql = "DELETE FROM student WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, studentId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public List<Map<String, Object>> fetchAllStudentsWithRegistrations(Connection connection) throws SQLException {
        String sql = "SELECT s.id, s.name, s.age, COALESCE(b.branch_name, s.branch) AS branch, COALESCE(c.course_name, r.course_name) AS course_name, r.fees_paid "
                + "FROM student s "
                + "LEFT JOIN branches b ON s.branch_id = b.branch_id "
                + "LEFT JOIN registration r ON s.id = r.student_id "
                + "LEFT JOIN courses c ON r.course_id = c.course_id OR r.course_name = c.course_name "
                + "ORDER BY s.id, r.reg_id";

        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", resultSet.getInt("id"));
                row.put("name", resultSet.getString("name"));
                row.put("age", resultSet.getInt("age"));
                row.put("branch", resultSet.getString("branch"));
                row.put("courseName", resultSet.getString("course_name"));
                row.put("feesPaid", resultSet.getDouble("fees_paid"));
                result.add(row);
            }
        }
        return result;
    }

    public List<Map<String, Object>> fetchHighPayingStudents(Connection connection, double minFee) throws SQLException {
        String sql = "SELECT s.id, s.name, s.age, COALESCE(b.branch_name, s.branch) AS branch, COALESCE(c.course_name, r.course_name) AS course_name, r.fees_paid "
                + "FROM student s "
                + "LEFT JOIN branches b ON s.branch_id = b.branch_id "
                + "JOIN registration r ON s.id = r.student_id "
                + "LEFT JOIN courses c ON r.course_id = c.course_id OR r.course_name = c.course_name "
                + "WHERE r.fees_paid > ? "
                + "ORDER BY r.fees_paid DESC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setDouble(1, minFee);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", resultSet.getInt("id"));
                    row.put("name", resultSet.getString("name"));
                    row.put("age", resultSet.getInt("age"));
                    row.put("branch", resultSet.getString("branch"));
                    row.put("courseName", resultSet.getString("course_name"));
                    row.put("feesPaid", resultSet.getDouble("fees_paid"));
                    result.add(row);
                }
            }
        }
        return result;
    }
}
