package org.example.services;

import org.example.dao.ReminderDao;
import org.example.model.Reminder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ReminderService {
    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);
    private final ReminderDao reminderDao;

    public ReminderService(ReminderDao reminderDao) {
        this.reminderDao = reminderDao;
        logger.debug("ReminderService initialized with ReminderDao");
    }

    public Reminder createReminder(Reminder reminder) throws ServiceException {
        try {
            logger.info("Creating new reminder for familyId: {}", reminder.getFamilyId());
            createReminder(reminder);

            reminderDao.addReminder(reminder);
            logger.info("Reminder created successfully with ID: {}", reminder.getId());

            return reminder;
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error while creating reminder: {}", e.getMessage());
            throw new ServiceException("Validation error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating reminder for familyId: " + reminder.getFamilyId(), e);
            throw new ServiceException("Failed to create reminder", e);
        }
    }

    public Reminder getReminderById(int id, int familyId, LocalDate startDate, LocalDate endDate) throws ServiceException {
        try {
            logger.debug("Getting reminder by ID: {}", id);
            Reminder reminder = (Reminder) reminderDao.getRemindersByFamilyAndDateRange(LocalDate.ofEpochDay(id), familyId, startDate, endDate);

            if (reminder == null) {
                logger.warn("Reminder not found with ID: {}", id);
            }

            return reminder;
        } catch (Exception e) {
            logger.error("Error getting reminder by ID: {}", id, e);
            throw new ServiceException("Failed to get reminder", e);
        }
    }

    public List<Reminder> getRemindersByFamily(int familyId, LocalDate startDate, LocalDate endDate) throws ServiceException {
        try {
            logger.debug("Getting reminders for familyId: {}", familyId);
            List<Reminder> reminders = reminderDao.getRemindersByFamilyAndDateRange(LocalDate.ofEpochDay(familyId), familyId, startDate, endDate);
            logger.info("Found {} reminders for familyId: {}", reminders.size(), familyId);
            return reminders;
        } catch (Exception e) {
            logger.error("Error getting reminders for familyId: {}", familyId, e);
            throw new ServiceException("Failed to get reminders", e);
        }
    }

    public List<Reminder> getUpcomingReminders(int familyId, int daysAhead, LocalDate startDate) throws ServiceException {
        try {
            if (daysAhead <= 0) {
                throw new IllegalArgumentException("Days ahead must be positive");
            }

            logger.debug("Getting upcoming reminders for familyId: {}, daysAhead: {}", familyId, daysAhead);
            LocalDate endDate = LocalDate.now().plusDays(daysAhead);
            List<Reminder> reminders = reminderDao.getRemindersByFamilyAndDateRange(endDate, familyId, startDate, endDate);

            reminders = reminders.stream()
                    .filter(r -> !r.isCompleted())
                    .collect(Collectors.toList());

            logger.info("Found {} upcoming reminders for familyId: {}", reminders.size(), familyId);
            return reminders;
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error in getUpcomingReminders: {}", e.getMessage());
            throw new ServiceException(e.getMessage());
        } catch (Exception e) {
            logger.error("Error getting upcoming reminders", e);
            throw new ServiceException("Failed to get upcoming reminders", e);
        }
    }

    // Остальные методы остаются без изменений...
    // (getActiveReminders, getOverdueReminders, getRemindersByType,
    // getActiveRemindersCount, updateReminder, deleteReminder, markAsCompleted,
    // reminderExists, validateReminder, validateReminderType)

    public static class ServiceException extends Exception {
        public ServiceException(String message) {
            super(message);
        }
        public ServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class NotFoundException extends Exception {
        public NotFoundException(String message) {
            super(message);
        }
    }
}