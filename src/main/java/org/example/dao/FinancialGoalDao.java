package org.example.dao;

import org.example.model.FinancialGoal;
import org.example.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinancialGoalDao {
    private Connection connection;

    public FinancialGoalDao() {
        this.connection = DatabaseConnection.getConnection();
    }

    /**
     * Добавляет новую финансовую цель в базу данных
     * @param goal объект финансовой цели для добавления
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public void addGoal(FinancialGoal goal) throws SQLException {
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
        }
    }

    /**
     * Получает финансовую цель по ID
     * @param id идентификатор цели
     * @return объект финансовой цели или null, если не найдена
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public FinancialGoal getGoalById(int id) throws SQLException {
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
        }
        return null;
    }

    /**
     * Получает все финансовые цели для указанной семьи
     * @param familyId идентификатор семьи
     * @return список финансовых целей семьи
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<FinancialGoal> getGoalsByFamily(int familyId) throws SQLException {
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
        }
        return goals;
    }

    /**
     * Обновляет информацию о финансовой цели
     * @param goal объект финансовой цели с обновленными данными
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public void updateGoal(FinancialGoal goal) throws SQLException {
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
        }
    }

    /**
     * Удаляет финансовую цель по ID
     * @param id идентификатор цели для удаления
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public void deleteGoal(int id) throws SQLException {
        String sql = "DELETE FROM financial_goals WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    /**
     * Добавляет сумму к текущему накоплению цели
     * @param goalId идентификатор цели
     * @param amount сумма для добавления
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public void addToCurrentAmount(int goalId, BigDecimal amount) throws SQLException {
        String sql = "UPDATE financial_goals SET current_amount = current_amount + ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, amount);
            statement.setInt(2, goalId);
            statement.executeUpdate();
        }
    }

    /**
     * Получает цели семьи с приоритетом
     * @param familyId идентификатор семьи
     * @param priority приоритет цели ("high", "medium", "low")
     * @return список целей с указанным приоритетом
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<FinancialGoal> getGoalsByFamilyAndPriority(int familyId, String priority) throws SQLException {
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
        }
        return goals;
    }

    /**
     * Получает цели семьи с близким сроком выполнения (в течение указанного количества дней)
     * @param familyId идентификатор семьи
     * @param days количество дней до срока выполнения
     * @return список целей с близким сроком
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<FinancialGoal> getUpcomingGoals(int familyId, int days) throws SQLException {
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
        }
        return goals;
    }

    /**
     * Получает завершенные цели семьи (где current_amount >= target_amount)
     * @param familyId идентификатор семьи
     * @return список завершенных целей
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<FinancialGoal> getCompletedGoals(int familyId) throws SQLException {
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
        }
        return goals;
    }

    /**
     * Получает общую сумму всех целевых накоплений семьи
     * @param familyId идентификатор семьи
     * @return общая сумма целей
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public BigDecimal getTotalTargetAmount(int familyId) throws SQLException {
        String sql = "SELECT SUM(target_amount) FROM financial_goals WHERE family_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal(1) != null ? resultSet.getBigDecimal(1) : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Получает общую сумму текущих накоплений семьи по целям
     * @param familyId идентификатор семьи
     * @return общая сумма накоплений
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public BigDecimal getTotalCurrentAmount(int familyId) throws SQLException {
        String sql = "SELECT SUM(current_amount) FROM financial_goals WHERE family_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal(1) != null ? resultSet.getBigDecimal(1) : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }
}
