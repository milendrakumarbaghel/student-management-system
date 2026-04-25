package dao;

import model.Branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BranchDAO {

    public boolean insertBranch(Connection connection, String branchName) throws SQLException {
        String sql = "INSERT INTO branches (branch_name) VALUES (?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, branchName);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean branchNameExists(Connection connection, String branchName) throws SQLException {
        String sql = "SELECT 1 FROM branches WHERE branch_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, branchName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public Branch findById(Connection connection, int branchId) throws SQLException {
        String sql = "SELECT branch_id, branch_name FROM branches WHERE branch_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, branchId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new Branch(resultSet.getInt("branch_id"), resultSet.getString("branch_name"));
            }
        }
    }

    public List<Branch> findAll(Connection connection) throws SQLException {
        String sql = "SELECT branch_id, branch_name FROM branches ORDER BY branch_name";
        List<Branch> branches = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                branches.add(new Branch(resultSet.getInt("branch_id"), resultSet.getString("branch_name")));
            }
        }

        return branches;
    }
}

