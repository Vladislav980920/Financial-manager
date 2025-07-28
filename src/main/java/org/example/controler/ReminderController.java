package org.example.controler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.example.model.Reminder;
import org.example.services.ReminderService;
import org.example.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReminderController {
    private static final Logger logger = LoggerFactory.getLogger(ReminderController.class);
    private final ReminderService reminderService;

    public ReminderController(io.javalin.Javalin app, ReminderService reminderService) {
        this.reminderService = reminderService;
        registerRoutes(app);
    }

    private void registerRoutes(io.javalin.Javalin app) {
        app.post("/api/families/{familyId}/reminders", this::createReminder);
        app.get("/api/families/{familyId}/reminders", this::getFamilyReminders);
        app.get("/api/families/{familyId}/reminders/upcoming/{daysAhead}", this::getUpcomingReminders);
        app.get("/api/families/{familyId}/reminders/active", this::getActiveReminders);
        app.get("/api/families/{familyId}/reminders/overdue", this::getOverdueReminders);
        app.get("/api/families/{familyId}/reminders/type/{type}", this::getRemindersByType);
        app.get("/api/families/{familyId}/reminders/range", this::getRemindersByDateRange);
        app.put("/api/reminders/{reminderId}/complete", this::markReminderAsCompleted);
        app.get("/api/families/{familyId}/reminders/active/count", this::getActiveRemindersCount);
    }

    private void getActiveRemindersCount(@NotNull Context context) {
    }

    private void markReminderAsCompleted(@NotNull Context context) {
        
    }

    private void getRemindersByDateRange(@NotNull Context context) {
        
    }

    private void getRemindersByType(@NotNull Context context) {
        
    }

    private void getOverdueReminders(@NotNull Context context) {
        
    }

    private void getActiveReminders(@NotNull Context context) {
        
    }

    private void createReminder(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            Reminder reminder = JsonUtil.fromJson(ctx.body(), Reminder.class);
            reminder.setFamilyId(familyId);

            Reminder createdReminder = reminderService.createReminder(reminder);
            ctx.status(HttpStatus.CREATED).json(createdReminder);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid family ID format"));
        } catch (ReminderService.ServiceException e) {
            logger.error("Error creating reminder", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void getFamilyReminders(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            List<Reminder> reminders = reminderService.getRemindersByFamily(familyId);
            ctx.json(reminders);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid family ID format"));
        } catch (ReminderService.ServiceException e) {
            logger.error("Error getting family reminders", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void getUpcomingReminders(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            int daysAhead = Integer.parseInt(ctx.pathParam("daysAhead"));

            List<Reminder> reminders = reminderService.getUpcomingReminders(familyId, daysAhead);
            ctx.json(reminders);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID or days format"));
        } catch (ReminderService.ServiceException e) {
            logger.error("Error getting upcoming reminders", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    // Остальные методы обработки маршрутов остаются без изменений...
    // (getActiveReminders, getOverdueReminders, getRemindersByType,
    // getRemindersByDateRange, markReminderAsCompleted, getActiveRemindersCount)
}