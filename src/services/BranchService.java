package services;

import dao.BranchDAO;
import exceptions.DatabaseOperationException;
import exceptions.DuplicateResourceException;
import exceptions.ValidationException;
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
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            String normalized = branchName.trim();
            if (branchDAO.branchNameExists(connection, normalized)) {
                throw new DuplicateResourceException("Duplicate branch name.");
            }
            return branchDAO.insertBranch(connection, normalized);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while adding branch.", e);
        }
    }

    public List<Branch> getAllBranches() {
        try (Connection connection = DBUtil.getConnection()) {
            return branchDAO.findAll(connection);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while fetching branches.", e);
        }
    }

    public Branch findBranchById(int branchId) {
        ValidationResult validationResult = BranchValidator.validateBranchId(branchId);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult.getMessage());
        }

        try (Connection connection = DBUtil.getConnection()) {
            return branchDAO.findById(connection, branchId);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error while searching branch.", e);
        }
    }

}

