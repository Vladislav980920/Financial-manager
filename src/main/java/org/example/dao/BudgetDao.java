package org.example.dao;

import lombok.SneakyThrows;
import org.example.model.Budget;
import org.example.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDao {
    private Connection connection;

    @SneakyThrows
    public BudgetDao() {
        this.connection = DatabaseConnection.getConnection();
    }

    public void addBudget(Budget budget) {
        String sql = "INSERT INTO budgets (family_id, category_id, limit_amount, period) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, budget.getFamilyId());
            statement.setInt(2, budget.getCategoryId());
            statement.setBigDecimal(3, budget.getLimitAmount());
            statement.setString(4, budget.getPeriod());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    budget.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding budget", e);
        }
    }

    public Budget getBudgetById(int id) {
        String sql = "SELECT * FROM budgets WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Budget(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("category_id"),
                            resultSet.getBigDecimal("limit_amount"),
                            resultSet.getString("period")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting budget by id", e);
        }
        return null;
    }

    public List<Budget> getBudgetsByFamily(int familyId) {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budgets WHERE family_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    budgets.add(new Budget(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("category_id"),
                            resultSet.getBigDecimal("limit_amount"),
                            resultSet.getString("period")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting budgets by family", e);
        }
        return budgets;
    }

    public Budget getBudgetByFamilyAndCategoryAndPeriod(int familyId, int categoryId, String period) {
        String sql = "SELECT * FROM budgets WHERE family_id = ? AND category_id = ? AND period = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setInt(2, categoryId);
            statement.setString(3, period);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Budget(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("category_id"),
                            resultSet.getBigDecimal("limit_amount"),
                            resultSet.getString("period")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting budget by family, category and period", e);
        }
        return null;
    }

    public void updateBudget(Budget budget) {
        String sql = "UPDATE budgets SET family_id = ?, category_id = ?, limit_amount = ?, period = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, budget.getFamilyId());
            statement.setInt(2, budget.getCategoryId());
            statement.setBigDecimal(3, budget.getLimitAmount());
            statement.setString(4, budget.getPeriod());
            statement.setInt(5, budget.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating budget", e);
        }
    }

    public void deleteBudget(int id) {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting budget", e);
        }
    }

    public boolean budgetExists(int familyId, int categoryId, String period) {
        String sql = "SELECT 1 FROM budgets WHERE family_id = ? AND category_id = ? AND period = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setInt(2, categoryId);
            statement.setString(3, period);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if budget exists", e);
        }
    }

    public BigDecimal getTotalBudgetAmountForFamily(int familyId, String period) {
        String sql = "SELECT SUM(limit_amount) FROM budgets WHERE family_id = ? AND period = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setString(2, period);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal(1) != null ? resultSet.getBigDecimal(1) : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total budget amount", e);
        }
        return BigDecimal.ZERO;
    }

    public List<Budget> getBudgetsByCategory(int categoryId) {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budgets WHERE category_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    budgets.add(new Budget(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("category_id"),
                            resultSet.getBigDecimal("limit_amount"),
                            resultSet.getString("period")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting budgets by category", e);
        }
        return budgets;
    }

    public List<Budget> getBudgetsByPeriod(String period) {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budgets WHERE period = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, period);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    budgets.add(new Budget(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("category_id"),
                            resultSet.getBigDecimal("limit_amount"),
                            resultSet.getString("period")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting budgets by period", e);
        }
        return budgets;
    }
}