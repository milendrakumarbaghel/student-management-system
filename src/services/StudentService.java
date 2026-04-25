package services;

import dao.BranchDAO;
import dao.RegistrationDAO;
import dao.StudentDAO;
import exceptions.DatabaseOperationException;
import exceptions.DuplicateResourceException;
import exceptions.ResourceNotFoundException;
import exceptions.ValidationException;
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
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            if (studentDAO.studentExists(connection, student.getId())) {
                throw new DuplicateResourceException("Duplicate student ID.");
            }

            Branch branch = branchDAO.findById(connection, student.getBranchId());
            if (branch == null) {
                throw new ResourceNotFoundException("Branch does not exist.");
            }

            student.setBranch(branch.getBranchName());
            return studentDAO.insertStudent(connection, student);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while adding student.", e);
        }
    }

    public List<Map<String, Object>> viewAllStudentsWithRegistrations() {
        try (Connection connection = DBUtil.getConnection()) {
            return studentDAO.fetchAllStudentsWithRegistrations(connection);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while fetching students.", e);
        }
    }

    public Student findStudentById(int studentId) {
        ValidationResult validationResult = StudentValidator.validateStudentId(studentId);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            return studentDAO.findById(connection, studentId);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while searching student.", e);
        }
    }

    public List<Registration> findRegistrationsByStudentId(int studentId) {
        ValidationResult validationResult = StudentValidator.validateStudentId(studentId);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            return registrationDAO.findByStudentId(connection, studentId);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while fetching registrations.", e);
        }
    }

    public boolean updateStudentName(int studentId, String name) {
        ValidationResult validationResult = StudentValidator.validateForNameUpdate(studentId, name);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            if (!studentDAO.studentExists(connection, studentId)) {
                throw new ResourceNotFoundException("Student does not exist.");
            }

            return studentDAO.updateStudentName(connection, studentId, name.trim());
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while updating student name.", e);
        }
    }

    public boolean updateStudentAge(int studentId, int age) {
        ValidationResult validationResult = StudentValidator.validateForAgeUpdate(studentId, age);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            if (!studentDAO.studentExists(connection, studentId)) {
                throw new ResourceNotFoundException("Student does not exist.");
            }

            return studentDAO.updateStudentAge(connection, studentId, age);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while updating student age.", e);
        }
    }

    public boolean updateStudentBranch(int studentId, int branchId) {
        ValidationResult validationResult = StudentValidator.validateForBranchUpdate(studentId, branchId);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!studentDAO.studentExists(connection, studentId)) {
                    throw new ResourceNotFoundException("Student does not exist.");
                }

                Branch branch = branchDAO.findById(connection, branchId);
                if (branch == null) {
                    throw new ResourceNotFoundException("Branch does not exist.");
                }

                boolean updated = studentDAO.updateStudentBranch(connection, studentId, branchId, branch.getBranchName());
                if (!updated) {
                    throw new DatabaseOperationException("Failed to update student branch.");
                }

                registrationDAO.deleteRegistrationsOutsideBranch(connection, studentId, branchId);
                registrationDAO.moveRegistrationsToBranchCourses(connection, studentId, branchId);

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw new DatabaseOperationException("Transaction failed while updating branch.", e);
            } catch (RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while updating student branch.", e);
        }
    }

    public boolean deleteStudent(int studentId) {
        ValidationResult validationResult = StudentValidator.validateStudentId(studentId);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!studentDAO.studentExists(connection, studentId)) {
                    throw new ResourceNotFoundException("Student does not exist.");
                }

                registrationDAO.deleteByStudentId(connection, studentId);
                boolean deleted = studentDAO.deleteStudent(connection, studentId);

                if (!deleted) {
                    throw new DatabaseOperationException("Failed to delete student.");
                }

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw new DatabaseOperationException("Transaction failed while deleting student.", e);
            } catch (RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while deleting student.", e);
        }
    }

    public List<Map<String, Object>> getHighPayingStudents(double minFee) {
        ValidationResult validationResult = StudentValidator.validateFeeThreshold(minFee);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            return studentDAO.fetchHighPayingStudents(connection, minFee);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while generating report.", e);
        }
    }

}
