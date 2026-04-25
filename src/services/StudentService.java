package services;

import dao.BranchDAO;
import dao.RegistrationDAO;
import dao.StudentDAO;
import model.Branch;
import model.Registration;
import model.Student;
import util.DBUtil;
import validation.StudentValidator;
import validation.ValidationResult;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StudentService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final RegistrationDAO registrationDAO = new RegistrationDAO();
    private final BranchDAO branchDAO = new BranchDAO();

    public boolean addStudent(Student student) {
        ValidationResult validationResult = StudentValidator.validateForAdd(student);
        if (!validationResult.isValid()) {
            System.out.println("Validation failed: " + validationResult.getMessage());
            return false;
        }

        try (Connection connection = DBUtil.getConnection()) {
            if (studentDAO.studentExists(connection, student.getId())) {
                System.out.println("Failure: duplicate student ID.");
                return false;
            }

            Branch branch = branchDAO.findById(connection, student.getBranchId());
            if (branch == null) {
                System.out.println("Failure: branch does not exist.");
                return false;
            }

            student.setBranch(branch.getBranchName());
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
        ValidationResult validationResult = StudentValidator.validateStudentId(studentId);
        if (!validationResult.isValid()) {
            System.out.println("Validation failed: " + validationResult.getMessage());
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
        ValidationResult validationResult = StudentValidator.validateStudentId(studentId);
        if (!validationResult.isValid()) {
            return Collections.emptyList();
        }

        try (Connection connection = DBUtil.getConnection()) {
            return registrationDAO.findByStudentId(connection, studentId);
        } catch (SQLException e) {
            System.out.println("Error while fetching registrations: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean updateStudentName(int studentId, String name) {
        ValidationResult validationResult = StudentValidator.validateForNameUpdate(studentId, name);
        if (!validationResult.isValid()) {
            System.out.println("Validation failed: " + validationResult.getMessage());
            return false;
        }

        try (Connection connection = DBUtil.getConnection()) {
            if (!studentDAO.studentExists(connection, studentId)) {
                System.out.println("Failure: student does not exist.");
                return false;
            }

            return studentDAO.updateStudentName(connection, studentId, name.trim());
        } catch (SQLException e) {
            System.out.println("Error while updating student name: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStudentAge(int studentId, int age) {
        ValidationResult validationResult = StudentValidator.validateForAgeUpdate(studentId, age);
        if (!validationResult.isValid()) {
            System.out.println("Validation failed: " + validationResult.getMessage());
            return false;
        }

        try (Connection connection = DBUtil.getConnection()) {
            if (!studentDAO.studentExists(connection, studentId)) {
                System.out.println("Failure: student does not exist.");
                return false;
            }

            return studentDAO.updateStudentAge(connection, studentId, age);
        } catch (SQLException e) {
            System.out.println("Error while updating student age: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStudentBranch(int studentId, int branchId) {
        ValidationResult validationResult = StudentValidator.validateForBranchUpdate(studentId, branchId);
        if (!validationResult.isValid()) {
            System.out.println("Validation failed: " + validationResult.getMessage());
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

                Branch branch = branchDAO.findById(connection, branchId);
                if (branch == null) {
                    System.out.println("Failure: branch does not exist.");
                    connection.rollback();
                    return false;
                }

                boolean updated = studentDAO.updateStudentBranch(connection, studentId, branchId, branch.getBranchName());
                if (!updated) {
                    connection.rollback();
                    return false;
                }

                registrationDAO.deleteRegistrationsOutsideBranch(connection, studentId, branchId);
                registrationDAO.moveRegistrationsToBranchCourses(connection, studentId, branchId);

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                System.out.println("Transaction failed while updating branch. Rolled back.");
                System.out.println("Error: " + e.getMessage());
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Error while updating student branch: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteStudent(int studentId) {
        ValidationResult validationResult = StudentValidator.validateStudentId(studentId);
        if (!validationResult.isValid()) {
            System.out.println("Validation failed: " + validationResult.getMessage());
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
        ValidationResult validationResult = StudentValidator.validateFeeThreshold(minFee);
        if (!validationResult.isValid()) {
            System.out.println("Validation failed: " + validationResult.getMessage());
            return Collections.emptyList();
        }

        try (Connection connection = DBUtil.getConnection()) {
            return studentDAO.fetchHighPayingStudents(connection, minFee);
        } catch (SQLException e) {
            System.out.println("Error while generating report: " + e.getMessage());
            return Collections.emptyList();
        }
    }

}
