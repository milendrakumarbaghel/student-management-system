package services;

import dao.CourseDAO;
import model.Course;
import util.DBUtil;
import validation.CourseValidator;
import validation.ValidationResult;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class CourseService {

    private final CourseDAO courseDAO = new CourseDAO();

    public boolean addCourse(int branchId, String courseName) {
        ValidationResult validationResult = CourseValidator.validateForAdd(branchId, courseName);
        if (!validationResult.isValid()) {
            System.out.println("Validation failed: " + validationResult.getMessage());
            return false;
        }

        try (Connection connection = DBUtil.getConnection()) {
            String normalized = courseName.trim();
            if (courseDAO.courseNameExists(connection, branchId, normalized)) {
                System.out.println("Failure: duplicate course name for this branch.");
                return false;
            }
            return courseDAO.insertCourse(connection, new Course(0, branchId, normalized));
        } catch (SQLException e) {
            System.out.println("Error while adding course: " + e.getMessage());
            return false;
        }
    }

    public List<Course> getAllCourses() {
        try (Connection connection = DBUtil.getConnection()) {
            return courseDAO.findAll(connection);
        } catch (SQLException e) {
            System.out.println("Error while fetching courses: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Course> getCoursesByBranchId(int branchId) {
        ValidationResult validationResult = CourseValidator.validateBranchId(branchId);
        if (!validationResult.isValid()) {
            System.out.println("Validation failed: " + validationResult.getMessage());
            return Collections.emptyList();
        }

        try (Connection connection = DBUtil.getConnection()) {
            return courseDAO.findByBranchId(connection, branchId);
        } catch (SQLException e) {
            System.out.println("Error while fetching courses by branch: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Course findCourseById(int courseId) {
        ValidationResult validationResult = CourseValidator.validateCourseId(courseId);
        if (!validationResult.isValid()) {
            System.out.println("Validation failed: " + validationResult.getMessage());
            return null;
        }

        try (Connection connection = DBUtil.getConnection()) {
            return courseDAO.findById(connection, courseId);
        } catch (SQLException e) {
            System.out.println("Error while searching course: " + e.getMessage());
            return null;
        }
    }

}

