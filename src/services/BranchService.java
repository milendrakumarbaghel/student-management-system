package services;

import dao.BranchDAO;
import model.Branch;
import util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class BranchService {

    private final BranchDAO branchDAO = new BranchDAO();

    public boolean addBranch(String branchName) {
        if (isBlank(branchName)) {
            System.out.println("Validation failed: branch name cannot be empty.");
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
        if (branchId <= 0) {
            System.out.println("Validation failed: branch ID must be positive.");
            return null;
        }

        try (Connection connection = DBUtil.getConnection()) {
            return branchDAO.findById(connection, branchId);
        } catch (SQLException e) {
            System.out.println("Error while searching branch: " + e.getMessage());
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

