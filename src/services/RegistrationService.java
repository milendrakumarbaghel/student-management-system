package services;

import dao.CourseDAO;
import dao.RegistrationDAO;
import dao.StudentDAO;
import exceptions.DatabaseOperationException;
import exceptions.DuplicateResourceException;
import exceptions.ResourceNotFoundException;
import exceptions.ValidationException;
import model.Course;
import model.Student;
import util.DBUtil;
import validation.RegistrationValidator;
import validation.ValidationResult;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

public class RegistrationService {

    private final RegistrationDAO registrationDAO = new RegistrationDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    public boolean registerForCourse(int studentId, int courseId, double fee) {
        ValidationResult validationResult = RegistrationValidator.validateForRegister(studentId, courseId, fee);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Course course = courseDAO.findById(connection, courseId);
                if (course == null) {
                    throw new ResourceNotFoundException("Course does not exist.");
                }

                Student student = studentDAO.findById(connection, studentId);
                if (student == null) {
                    throw new ResourceNotFoundException("Student does not exist.");
                }

                if (course.getBranchId() != student.getBranchId()) {
                    throw new ValidationException("Selected course does not belong to the student's branch.");
                }

                if (registrationDAO.registrationExists(connection, studentId, courseId, course.getCourseName())) {
                    throw new DuplicateResourceException("Duplicate registration for this student and course.");
                }

                boolean inserted = registrationDAO.insertRegistration(connection, studentId, courseId, course.getCourseName(), fee);
                if (!inserted) {
                    throw new DatabaseOperationException("Failed to insert registration.");
                }

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw new DatabaseOperationException("Transaction failed during registration.", e);
            } catch (RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while registering course.", e);
        }
    }

    public boolean updateCourseFee(int studentId, double fee) {
        ValidationResult validationResult = RegistrationValidator.validateForFeeUpdate(studentId, fee);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            if (!studentDAO.studentExists(connection, studentId)) {
                throw new ResourceNotFoundException("Student does not exist.");
            }
            return registrationDAO.updateCourseFeeByStudentId(connection, studentId, fee);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while updating fee.", e);
        }
    }

    public boolean cancelRegistration(int studentId, int courseId) {
        ValidationResult validationResult = RegistrationValidator.validateForCancel(studentId, courseId);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            Course course = courseDAO.findById(connection, courseId);
            if (course == null) {
                throw new ResourceNotFoundException("Course does not exist.");
            }
            return registrationDAO.cancelRegistration(connection, studentId, courseId, course.getCourseName());
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while cancelling registration.", e);
        }
    }

    public Map<String, Integer> getCourseWiseStudentCount() {
        try (Connection connection = DBUtil.getConnection()) {
            return registrationDAO.fetchCourseWiseCount(connection);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while generating course-wise report.", e);
        }
    }
}
