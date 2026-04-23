package services;

import dao.RegistrationDAO;
import dao.StudentDAO;
import model.Registration;
import model.Student;
import util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StudentService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final RegistrationDAO registrationDAO = new RegistrationDAO();

    public boolean addStudent(Student student) {
        if (student == null || student.getId() <= 0 || isBlank(student.getName()) || student.getAge() <= 0 || isBlank(student.getBranch())) {
            System.out.println("Validation failed: ID, name, age and branch must be valid.");
            return false;
        }

        try (Connection connection = DBUtil.getConnection()) {
            if (studentDAO.studentExists(connection, student.getId())) {
                System.out.println("Failure: duplicate student ID.");
                return false;
            }
            return studentDAO.insertStudent(connection, student);
        } catch (SQLException e) {
            System.out.println("Error while adding student: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> viewAllStudentsWithRegistrations() {
        try (Connection connection = DBUtil.getConnection()) {
            return studentDAO.fetchAllStudentsWithRegistrations(connection);
        } catch (SQLException e) {
            System.out.println("Error while fetching students: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Student findStudentById(int studentId) {
        if (studentId <= 0) {
            System.out.println("Validation failed: student ID must be positive.");
            return null;
        }

        try (Connection connection = DBUtil.getConnection()) {
            return studentDAO.findById(connection, studentId);
        } catch (SQLException e) {
            System.out.println("Error while searching student: " + e.getMessage());
            return null;
        }
    }

    public List<Registration> findRegistrationsByStudentId(int studentId) {
        if (studentId <= 0) {
            return Collections.emptyList();
        }

        try (Connection connection = DBUtil.getConnection()) {
            return registrationDAO.findByStudentId(connection, studentId);
        } catch (SQLException e) {
            System.out.println("Error while fetching registrations: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean updateStudent(int studentId, String name, String branch) {
        if (studentId <= 0 || isBlank(name) || isBlank(branch)) {
            System.out.println("Validation failed: student ID, name and branch are required.");
            return false;
        }

        try (Connection connection = DBUtil.getConnection()) {
            if (!studentDAO.studentExists(connection, studentId)) {
                System.out.println("Failure: student does not exist.");
                return false;
            }
            return studentDAO.updateStudent(connection, studentId, name.trim(), branch.trim());
        } catch (SQLException e) {
            System.out.println("Error while updating student: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteStudent(int studentId) {
        if (studentId <= 0) {
            System.out.println("Validation failed: student ID must be positive.");
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

                registrationDAO.deleteByStudentId(connection, studentId);
                boolean deleted = studentDAO.deleteStudent(connection, studentId);

                if (!deleted) {
                    connection.rollback();
                    return false;
                }

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                System.out.println("Transaction failed while deleting student. Rolled back.");
                System.out.println("Error: " + e.getMessage());
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Error while deleting student: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getHighPayingStudents(double minFee) {
        if (minFee <= 0) {
            System.out.println("Validation failed: fee threshold must be positive.");
            return Collections.emptyList();
        }

        try (Connection connection = DBUtil.getConnection()) {
            return studentDAO.fetchHighPayingStudents(connection, minFee);
        } catch (SQLException e) {
            System.out.println("Error while generating report: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
