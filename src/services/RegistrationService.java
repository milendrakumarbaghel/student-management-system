package services;

import dao.RegistrationDAO;
import dao.StudentDAO;
import util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

public class RegistrationService {

    private final RegistrationDAO registrationDAO = new RegistrationDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    public boolean registerForCourse(int studentId, String courseName, double fee) {
        if (studentId <= 0 || isBlank(courseName) || fee <= 0) {
            System.out.println("Validation failed: student ID, course and positive fee are required.");
            return false;
        }

        try (Connection connection = DBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!studentDAO.studentExists(connection, studentId)) {
                    System.out.println("Failure: student does not exist.");
                    connection.rollback();
                    return false;
                }

                if (registrationDAO.registrationExists(connection, studentId, courseName.trim())) {
                    System.out.println("Failure: duplicate registration for this student and course.");
                    connection.rollback();
                    return false;
                }

                boolean inserted = registrationDAO.insertRegistration(connection, studentId, courseName.trim(), fee);
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

    public boolean updateCourseFee(int studentId, String courseName, double fee) {
        if (studentId <= 0 || isBlank(courseName) || fee <= 0) {
            System.out.println("Validation failed: student ID, course and positive fee are required.");
            return false;
        }

        try (Connection connection = DBUtil.getConnection()) {
            return registrationDAO.updateCourseFee(connection, studentId, courseName.trim(), fee);
        } catch (SQLException e) {
            System.out.println("Error while updating fee: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelRegistration(int studentId, String courseName) {
        if (studentId <= 0 || isBlank(courseName)) {
            System.out.println("Validation failed: student ID and course are required.");
            return false;
        }

        try (Connection connection = DBUtil.getConnection()) {
            return registrationDAO.cancelRegistration(connection, studentId, courseName.trim());
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
