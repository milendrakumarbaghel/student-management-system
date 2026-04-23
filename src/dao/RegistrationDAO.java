package dao;

import model.Registration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegistrationDAO {

    public boolean registrationExists(Connection connection, int studentId, String courseName) throws SQLException {
        String sql = "SELECT 1 FROM registration WHERE student_id = ? AND course_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, studentId);
            preparedStatement.setString(2, courseName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean insertRegistration(Connection connection, int studentId, String courseName, double fee) throws SQLException {
        String sql = "INSERT INTO registration (student_id, course_name, fees_paid) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, studentId);
            preparedStatement.setString(2, courseName);
            preparedStatement.setDouble(3, fee);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public List<Registration> findByStudentId(Connection connection, int studentId) throws SQLException {
        String sql = "SELECT reg_id, student_id, course_name, fees_paid FROM registration WHERE student_id = ? ORDER BY reg_id";
        List<Registration> registrations = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, studentId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    registrations.add(new Registration(
                            resultSet.getInt("reg_id"),
                            resultSet.getInt("student_id"),
                            resultSet.getString("course_name"),
                            resultSet.getDouble("fees_paid")
                    ));
                }
            }
        }

        return registrations;
    }

    public boolean updateCourseFee(Connection connection, int studentId, String courseName, double fee) throws SQLException {
        String sql = "UPDATE registration SET fees_paid = ? WHERE student_id = ? AND course_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setDouble(1, fee);
            preparedStatement.setInt(2, studentId);
            preparedStatement.setString(3, courseName);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean cancelRegistration(Connection connection, int studentId, String courseName) throws SQLException {
        String sql = "DELETE FROM registration WHERE student_id = ? AND course_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, studentId);
            preparedStatement.setString(2, courseName);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public int deleteByStudentId(Connection connection, int studentId) throws SQLException {
        String sql = "DELETE FROM registration WHERE student_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, studentId);
            return preparedStatement.executeUpdate();
        }
    }

    public Map<String, Integer> fetchCourseWiseCount(Connection connection) throws SQLException {
        String sql = "SELECT course_name, COUNT(DISTINCT student_id) AS student_count "
                + "FROM registration GROUP BY course_name ORDER BY course_name";

        Map<String, Integer> result = new LinkedHashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                result.put(resultSet.getString("course_name"), resultSet.getInt("student_count"));
            }
        }

        return result;
    }
}
