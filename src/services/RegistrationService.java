package services;

import dao.CourseDAO;
import dao.RegistrationDAO;
import dao.StudentDAO;
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
            System.out.println("Validation failed: " + validationResult.getMessage());
            return false;
        }

        try (Connection connection = DBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Course course = courseDAO.findById(connection, courseId);
                if (course == null) {
                    System.out.println("Failure: course does not exist.");
                    connection.rollback();
                    return false;
                }

                Student student = studentDAO.findById(connection, studentId);
                if (student == null) {
                    System.out.println("Failure: student does not exist.");
                    connection.rollback();
                    return false;
                }

                if (course.getBranchId() != student.getBranchId()) {
                    System.out.println("Failure: selected course does not belong to the student's branch.");
                    connection.rollback();
                    return false;
                }

                if (registrationDAO.registrationExists(connection, studentId, courseId, course.getCourseName())) {
                    System.out.println("Failure: duplicate registration for this student and course.");
                    connection.rollback();
                    return false;
                }

                boolean inserted = registrationDAO.insertRegistration(connection, studentId, courseId, course.getCourseName(), fee);
                if (!inserted) {
                    connection.rollback();
                    return false;
                }

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                System.out.println("Transaction failed during registration. Rolled back.");
                System.out.println("Error: " + e.getMessage());
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Error while registering course: " + e.getMessage());
            return false;
        }
    }

    public boolean updateCourseFee(int studentId, double fee) {
        ValidationResult validationResult = RegistrationValidator.validateForFeeUpdate(studentId, fee);
        if (!validationResult.isValid()) {
            System.out.println("Validation failed: " + validationResult.getMessage());
            return false;
        }

        try (Connection connection = DBUtil.getConnection()) {
            if (!studentDAO.studentExists(connection, studentId)) {
                System.out.println("Failure: student does not exist.");
                return false;
            }
            return registrationDAO.updateCourseFeeByStudentId(connection, studentId, fee);
        } catch (SQLException e) {
            System.out.println("Error while updating fee: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelRegistration(int studentId, int courseId) {
        ValidationResult validationResult = RegistrationValidator.validateForCancel(studentId, courseId);
        if (!validationResult.isValid()) {
            System.out.println("Validation failed: " + validationResult.getMessage());
            return false;
        }

        try (Connection connection = DBUtil.getConnection()) {
            Course course = courseDAO.findById(connection, courseId);
            if (course == null) {
                System.out.println("Failure: course does not exist.");
                return false;
            }
            return registrationDAO.cancelRegistration(connection, studentId, courseId, course.getCourseName());
        } catch (SQLException e) {
            System.out.println("Error while cancelling registration: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Integer> getCourseWiseStudentCount() {
        try (Connection connection = DBUtil.getConnection()) {
            return registrationDAO.fetchCourseWiseCount(connection);
        } catch (SQLException e) {
            System.out.println("Error while generating course-wise report: " + e.getMessage());
            return Collections.emptyMap();
        }
    }
}
