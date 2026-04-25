package dao;

import model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public boolean insertCourse(Connection connection, Course course) throws SQLException {
        String sql = "INSERT INTO courses (branch_id, course_name) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, course.getBranchId());
            preparedStatement.setString(2, course.getCourseName());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean courseNameExists(Connection connection, int branchId, String courseName) throws SQLException {
        String sql = "SELECT 1 FROM courses WHERE branch_id = ? AND course_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, branchId);
            preparedStatement.setString(2, courseName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public Course findById(Connection connection, int courseId) throws SQLException {
        String sql = "SELECT course_id, branch_id, course_name FROM courses WHERE course_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, courseId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new Course(
                        resultSet.getInt("course_id"),
                        resultSet.getInt("branch_id"),
                        resultSet.getString("course_name")
                );
            }
        }
    }

    public List<Course> findAll(Connection connection) throws SQLException {
        String sql = "SELECT course_id, branch_id, course_name FROM courses ORDER BY course_name";
        return findByQuery(connection, sql, null);
    }

    public List<Course> findByBranchId(Connection connection, int branchId) throws SQLException {
        String sql = "SELECT course_id, branch_id, course_name FROM courses WHERE branch_id = ? ORDER BY course_name";
        return findByQuery(connection, sql, branchId);
    }

    private List<Course> findByQuery(Connection connection, String sql, Integer branchId) throws SQLException {
        List<Course> courses = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            if (branchId != null) {
                preparedStatement.setInt(1, branchId);
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                courses.add(new Course(
                        resultSet.getInt("course_id"),
                        resultSet.getInt("branch_id"),
                        resultSet.getString("course_name")
                ));
            }
            }
        }

        return courses;
    }
}

