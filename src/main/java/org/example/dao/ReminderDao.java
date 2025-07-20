package org.example.dao;

import org.example.model.Reminder;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReminderDao {
    private Connection connection;

    public ReminderDao() {
        this.connection = DatabaseConnection.getConnection();
    }

    /**
     * Добавляет новое напоминание в базу данных
     * @param reminder объект напоминания для добавления
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public void addReminder(Reminder reminder) throws SQLException {
        String sql = "INSERT INTO reminders (family_id, title, description, due_date, is_completed, type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, reminder.getFamilyId());
            statement.setString(2, reminder.getTitle());
            statement.setString(3, reminder.getDescription());
            statement.setDate(4, Date.valueOf(reminder.getDueDate()));
            statement.setBoolean(5, reminder.isCompleted());
            statement.setString(6, reminder.getType());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reminder.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Получает напоминание по ID
     * @param id идентификатор напоминания
     * @return объект напоминания или null, если не найдено
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public Reminder getReminderById(int id) throws SQLException {
        String sql = "SELECT * FROM reminders WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Reminder(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getString("title"),
                            resultSet.getString("description"),
                            resultSet.getDate("due_date").toLocalDate(),
                            resultSet.getBoolean("is_completed"),
                            resultSet.getString("type")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Получает все напоминания для указанной семьи
     * @param familyId идентификатор семьи
     * @return список напоминаний семьи
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<Reminder> getRemindersByFamily(int familyId) throws SQLException {
        List<Reminder> reminders = new ArrayList<>();
        String sql = "SELECT * FROM reminders WHERE family_id = ? ORDER BY due_date ASC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reminders.add(new Reminder(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getString("title"),
                            resultSet.getString("description"),
                            resultSet.getDate("due_date").toLocalDate(),
                            resultSet.getBoolean("is_completed"),
                            resultSet.getString("type")
                    ));
                }
            }
        }
        return reminders;
    }

    /**
     * Обновляет информацию о напоминании
     * @param reminder объект напоминания с обновленными данными
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public void updateReminder(Reminder reminder) throws SQLException {
        String sql = "UPDATE reminders SET family_id = ?, title = ?, description = ?, " +
                "due_date = ?, is_completed = ?, type = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reminder.getFamilyId());
            statement.setString(2, reminder.getTitle());
            statement.setString(3, reminder.getDescription());
            statement.setDate(4, Date.valueOf(reminder.getDueDate()));
            statement.setBoolean(5, reminder.isCompleted());
            statement.setString(6, reminder.getType());
            statement.setInt(7, reminder.getId());

            statement.executeUpdate();
        }
    }

    /**
     * Удаляет напоминание по ID
     * @param id идентификатор напоминания для удаления
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public void deleteReminder(int id) throws SQLException {
        String sql = "DELETE FROM reminders WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    /**
     * Помечает напоминание как выполненное
     * @param id идентификатор напоминания
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public void markAsCompleted(int id) throws SQLException {
        String sql = "UPDATE reminders SET is_completed = true WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    /**
     * Получает напоминания семьи за указанный период
     * @param familyId идентификатор семьи
     * @param startDate начальная дата периода
     * @param endDate конечная дата периода
     * @return список напоминаний за период
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<Reminder> getRemindersByFamilyAndDateRange(int familyId, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Reminder> reminders = new ArrayList<>();
        String sql = "SELECT * FROM reminders WHERE family_id = ? AND due_date BETWEEN ? AND ? ORDER BY due_date ASC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setDate(2, Date.valueOf(startDate));
            statement.setDate(3, Date.valueOf(endDate));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reminders.add(new Reminder(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getString("title"),
                            resultSet.getString("description"),
                            resultSet.getDate("due_date").toLocalDate(),
                            resultSet.getBoolean("is_completed"),
                            resultSet.getString("type")
                    ));
                }
            }
        }
        return reminders;
    }

    /**
     * Получает активные (не выполненные) напоминания семьи
     * @param familyId идентификатор семьи
     * @return список активных напоминаний
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<Reminder> getActiveReminders(int familyId) throws SQLException {
        List<Reminder> reminders = new ArrayList<>();
        String sql = "SELECT * FROM reminders WHERE family_id = ? AND is_completed = false ORDER BY due_date ASC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reminders.add(new Reminder(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getString("title"),
                            resultSet.getString("description"),
                            resultSet.getDate("due_date").toLocalDate(),
                            resultSet.getBoolean("is_completed"),
                            resultSet.getString("type")
                    ));
                }
            }
        }
        return reminders;
    }

    /**
     * Получает напоминания по типу
     * @param familyId идентификатор семьи
     * @param type тип напоминания ("payment", "budget", "goal" и т.д.)
     * @return список напоминаний указанного типа
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<Reminder> getRemindersByType(int familyId, String type) throws SQLException {
        List<Reminder> reminders = new ArrayList<>();
        String sql = "SELECT * FROM reminders WHERE family_id = ? AND type = ? ORDER BY due_date ASC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setString(2, type);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reminders.add(new Reminder(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getString("title"),
                            resultSet.getString("description"),
                            resultSet.getDate("due_date").toLocalDate(),
                            resultSet.getBoolean("is_completed"),
                            resultSet.getString("type")
                    ));
                }
            }
        }
        return reminders;
    }

    /**
     * Получает просроченные напоминания семьи
     * @param familyId идентификатор семьи
     * @return список просроченных напоминаний
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<Reminder> getOverdueReminders(int familyId) throws SQLException {
        List<Reminder> reminders = new ArrayList<>();
        LocalDate today = LocalDate.now();
        String sql = "SELECT * FROM reminders WHERE family_id = ? AND due_date < ? AND is_completed = false ORDER BY due_date ASC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setDate(2, Date.valueOf(today));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reminders.add(new Reminder(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getString("title"),
                            resultSet.getString("description"),
                            resultSet.getDate("due_date").toLocalDate(),
                            resultSet.getBoolean("is_completed"),
                            resultSet.getString("type")
                    ));
                }
            }
        }
        return reminders;
    }

    /**
     * Получает количество активных напоминаний семьи
     * @param familyId идентификатор семьи
     * @return количество активных напоминаний
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public int getActiveRemindersCount(int familyId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reminders WHERE family_id = ? AND is_completed = false";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }
}
