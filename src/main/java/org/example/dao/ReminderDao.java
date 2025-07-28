package org.example.dao;

import lombok.SneakyThrows;
import org.example.model.Reminder;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReminderDao {
    private Connection connection;

    @SneakyThrows
    public ReminderDao() {
        this.connection = DatabaseConnection.getConnection();
    }

    public void addReminder(Reminder reminder) {
        if (reminder == null) {
            throw new IllegalArgumentException("Reminder cannot be null");
        }

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
        } catch (SQLException e) {
            throw new RuntimeException("Error adding reminder", e);
        }
    }

    public List<Reminder> getRemindersByFamilyAndDateRange(LocalDate id, int familyId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }

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
        } catch (SQLException e) {
            throw new RuntimeException("Error getting reminders by date range", e);
        }
        return reminders;
    }

    // Остальные методы остаются без изменений...
    // (getReminderById, getRemindersByFamily, updateReminder, deleteReminder,
    // markAsCompleted, getActiveReminders, getRemindersByType, getOverdueReminders,
    // getActiveRemindersCount)
}