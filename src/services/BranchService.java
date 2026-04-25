package services;

import dao.BranchDAO;
import model.Branch;
import util.DBUtil;
import validation.BranchValidator;
import validation.ValidationResult;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class BranchService {

    private final BranchDAO branchDAO = new BranchDAO();

    public boolean addBranch(String branchName) {
        ValidationResult validationResult = BranchValidator.validateBranchName(branchName);
        if (!validationResult.isValid()) {
            System.out.println("Validation failed: " + validationResult.getMessage());
            return false;
        }

        try (Connection connection = DBUtil.getConnection()) {
            String normalized = branchName.trim();
            if (branchDAO.branchNameExists(connection, normalized)) {
                System.out.println("Failure: duplicate branch name.");
                return false;
            }
            return branchDAO.insertBranch(connection, normalized);
        } catch (SQLException e) {
            System.out.println("Error while adding branch: " + e.getMessage());
            return false;
        }
    }

    public List<Branch> getAllBranches() {
        try (Connection connection = DBUtil.getConnection()) {
            return branchDAO.findAll(connection);
        } catch (SQLException e) {
            System.out.println("Error while fetching branches: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Branch findBranchById(int branchId) {
        ValidationResult validationResult = BranchValidator.validateBranchId(branchId);
        if (!validationResult.isValid()) {
            System.out.println("Validation failed: " + validationResult.getMessage());
            return null;
        }

        try (Connection connection = DBUtil.getConnection()) {
            return branchDAO.findById(connection, branchId);
        } catch (SQLException e) {
            System.out.println("Error while searching branch: " + e.getMessage());
            return null;
        }
    }

}

