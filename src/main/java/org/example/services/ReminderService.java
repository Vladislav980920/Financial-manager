package org.example.services;

import org.example.dao.ReminderDao;
import org.example.model.Reminder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReminderService {
    private ReminderDao reminderDao;

    public ReminderService(ReminderDao reminderDao) {
        this.reminderDao = reminderDao;
    }

    public void createReminder(int familyId, String title, String description,
                               LocalDate dueDate, String type) throws SQLException {
        Reminder reminder = new Reminder(0, familyId, title, description, dueDate, false, type);
        reminderDao.addReminder(reminder);
    }

    public List<Reminder> getUpcomingReminders(int familyId, int daysAhead) throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);
        return reminderDao.getRemindersByFamilyAndDateRange(familyId, today, endDate);
    }

    public void markReminderAsCompleted(int reminderId) throws SQLException {
        Reminder reminder = reminderDao.getReminderById(reminderId);
        if (reminder != null) {
            reminder.setCompleted(true);
            reminderDao.updateReminder(reminder);
        }
    }
}
