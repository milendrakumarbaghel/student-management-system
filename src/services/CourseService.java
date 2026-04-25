package services;

import dao.CourseDAO;
import exceptions.DatabaseOperationException;
import exceptions.DuplicateResourceException;
import exceptions.ValidationException;
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
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            String normalized = courseName.trim();
            if (courseDAO.courseNameExists(connection, branchId, normalized)) {
                throw new DuplicateResourceException("Duplicate course name for this branch.");
            }
            return courseDAO.insertCourse(connection, new Course(0, branchId, normalized));
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while adding course.", e);
        }
    }

    public List<Course> getAllCourses() {
        try (Connection connection = DBUtil.getConnection()) {
            return courseDAO.findAll(connection);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while fetching courses.", e);
        }
    }

    public List<Course> getCoursesByBranchId(int branchId) {
        ValidationResult validationResult = CourseValidator.validateBranchId(branchId);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            return courseDAO.findByBranchId(connection, branchId);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while fetching courses by branch.", e);
        }
    }

    public Course findCourseById(int courseId) {
        ValidationResult validationResult = CourseValidator.validateCourseId(courseId);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            return courseDAO.findById(connection, courseId);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while searching course.", e);
        }
    }

}

