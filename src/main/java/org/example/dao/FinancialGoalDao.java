package org.example.dao;

import lombok.SneakyThrows;
import org.example.model.FinancialGoal;
import org.example.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinancialGoalDao {
    private Connection connection;

    @SneakyThrows
    public FinancialGoalDao() {
        this.connection = DatabaseConnection.getConnection();
    }

    public void addGoal(FinancialGoal goal) {
        String sql = "INSERT INTO financial_goals (family_id, name, description, target_amount, " +
                "current_amount, target_date, priority) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, goal.getFamilyId());
            statement.setString(2, goal.getName());
            statement.setString(3, goal.getDescription());
            statement.setBigDecimal(4, goal.getTargetAmount());
            statement.setBigDecimal(5, goal.getCurrentAmount());
            statement.setDate(6, Date.valueOf(goal.getTargetDate()));
            statement.setString(7, goal.getPriority());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    goal.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding financial goal", e);
        }
    }

    public FinancialGoal getGoalById(int id) {
        String sql = "SELECT * FROM financial_goals WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new FinancialGoal(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getBigDecimal("target_amount"),
                            resultSet.getBigDecimal("current_amount"),
                            resultSet.getDate("target_date").toLocalDate(),
                            resultSet.getString("priority")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting financial goal by id", e);
        }
        return null;
    }

    public List<FinancialGoal> getGoalsByFamily(int familyId) {
        List<FinancialGoal> goals = new ArrayList<>();
        String sql = "SELECT * FROM financial_goals WHERE family_id = ? ORDER BY target_date ASC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    goals.add(new FinancialGoal(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getBigDecimal("target_amount"),
                            resultSet.getBigDecimal("current_amount"),
                            resultSet.getDate("target_date").toLocalDate(),
                            resultSet.getString("priority")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting goals by family", e);
        }
        return goals;
    }

    public void updateGoal(FinancialGoal goal) {
        String sql = "UPDATE financial_goals SET family_id = ?, name = ?, description = ?, " +
                "target_amount = ?, current_amount = ?, target_date = ?, priority = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, goal.getFamilyId());
            statement.setString(2, goal.getName());
            statement.setString(3, goal.getDescription());
            statement.setBigDecimal(4, goal.getTargetAmount());
            statement.setBigDecimal(5, goal.getCurrentAmount());
            statement.setDate(6, Date.valueOf(goal.getTargetDate()));
            statement.setString(7, goal.getPriority());
            statement.setInt(8, goal.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating financial goal", e);
        }
    }

    public void deleteGoal(int id) {
        String sql = "DELETE FROM financial_goals WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting financial goal", e);
        }
    }

    public void addToCurrentAmount(int goalId, BigDecimal amount) {
        String sql = "UPDATE financial_goals SET current_amount = current_amount + ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, amount);
            statement.setInt(2, goalId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding to current amount", e);
        }
    }

    public List<FinancialGoal> getGoalsByFamilyAndPriority(int familyId, String priority) {
        List<FinancialGoal> goals = new ArrayList<>();
        String sql = "SELECT * FROM financial_goals WHERE family_id = ? AND priority = ? ORDER BY target_date ASC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setString(2, priority);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    goals.add(new FinancialGoal(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getBigDecimal("target_amount"),
                            resultSet.getBigDecimal("current_amount"),
                            resultSet.getDate("target_date").toLocalDate(),
                            resultSet.getString("priority")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting goals by family and priority", e);
        }
        return goals;
    }

    public List<FinancialGoal> getUpcomingGoals(int familyId, int days) {
        List<FinancialGoal> goals = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(days);

        String sql = "SELECT * FROM financial_goals WHERE family_id = ? AND target_date BETWEEN ? AND ? ORDER BY target_date ASC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setDate(2, Date.valueOf(today));
            statement.setDate(3, Date.valueOf(targetDate));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    goals.add(new FinancialGoal(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getBigDecimal("target_amount"),
                            resultSet.getBigDecimal("current_amount"),
                            resultSet.getDate("target_date").toLocalDate(),
                            resultSet.getString("priority")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting upcoming goals", e);
        }
        return goals;
    }

    public List<FinancialGoal> getCompletedGoals(int familyId) {
        List<FinancialGoal> goals = new ArrayList<>();
        String sql = "SELECT * FROM financial_goals WHERE family_id = ? AND current_amount >= target_amount ORDER BY target_date ASC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    goals.add(new FinancialGoal(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getBigDecimal("target_amount"),
                            resultSet.getBigDecimal("current_amount"),
                            resultSet.getDate("target_date").toLocalDate(),
                            resultSet.getString("priority")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting completed goals", e);
        }
        return goals;
    }

    public BigDecimal getTotalTargetAmount(int familyId) {
        String sql = "SELECT SUM(target_amount) FROM financial_goals WHERE family_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal(1) != null ? resultSet.getBigDecimal(1) : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total target amount", e);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTotalCurrentAmount(int familyId) {
        String sql = "SELECT SUM(current_amount) FROM financial_goals WHERE family_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal(1) != null ? resultSet.getBigDecimal(1) : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total current amount", e);
        }
        return BigDecimal.ZERO;
    }
}