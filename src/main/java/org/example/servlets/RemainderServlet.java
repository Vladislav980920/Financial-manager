package org.example.servlets;

import org.example.model.Reminder;
import org.example.services.ReminderService;
import org.example.services.ReminderService.ServiceException;
import org.example.services.ReminderService.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/api/families/*/reminders")
public class ReminderServlet extends HttpServlet {
    private ReminderService reminderService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        this.reminderService = new ReminderService(new org.example.dao.ReminderDao());
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            String[] pathParts = pathInfo.split("/");

            if (pathParts.length == 3) {
                int familyId = Integer.parseInt(pathParts[1]);
                handleListReminders(familyId, req, resp);
            } else if (pathParts.length == 5 && pathParts[3].equals("reminders")) {
                int familyId = Integer.parseInt(pathParts[1]);
                int reminderId = Integer.parseInt(pathParts[4]);
                handleSingleReminder(familyId, reminderId, resp);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (NotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            String[] pathParts = pathInfo.split("/");

            if (pathParts.length != 3) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
                return;
            }

            int familyId = Integer.parseInt(pathParts[1]);
            Reminder reminder = objectMapper.readValue(req.getInputStream(), Reminder.class);
            reminder.setFamilyId(familyId);
            validateReminder(reminder);

            Reminder createdReminder = reminderService.createReminder(reminder);
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, createdReminder);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create reminder");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            String[] pathParts = pathInfo.split("/");

            if (pathParts.length != 5 || !pathParts[3].equals("reminders")) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
                return;
            }

            int familyId = Integer.parseInt(pathParts[1]);
            int reminderId = Integer.parseInt(pathParts[4]);
            Reminder reminder = objectMapper.readValue(req.getInputStream(), Reminder.class);
            reminder.setId(reminderId);
            reminder.setFamilyId(familyId);
            validateReminder(reminder);

            Reminder updatedReminder = reminderService.createReminder(reminder);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, updatedReminder);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update reminder");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            String[] pathParts = pathInfo.split("/");

            if (pathParts.length != 5 || !pathParts[3].equals("reminders")) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
                return;
            }

            int familyId = Integer.parseInt(pathParts[1]);
            int reminderId = Integer.parseInt(pathParts[4]);


            Reminder reminder = reminderService.getReminderById(reminderId, familyId, null, null);
            if (reminder == null) {
                throw new NotFoundException("Reminder not found in this family");
            }

            new org.example.dao.ReminderDao().deleteReminder(reminderId);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (NotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete reminder");
        }
    }

    private void handleListReminders(int familyId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String upcomingDays = req.getParameter("upcomingDays");
        String type = req.getParameter("type");
        String completed = req.getParameter("completed");

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = null;

        if (upcomingDays != null) {
            int days = Integer.parseInt(upcomingDays);
            endDate = startDate.plusDays(days);
        }

        List<Reminder> reminders;

        if (upcomingDays != null) {
            reminders = reminderService.getUpcomingReminders(familyId, Integer.parseInt(upcomingDays), startDate);
        } else if (type != null) {

            reminders = new org.example.dao.ReminderDao()
                    .getRemindersByFamilyAndDateRange(startDate, familyId, startDate, endDate)
                    .stream()
                    .filter(r -> r.getType().equals(type))
                    .collect(Collectors.toList());
        } else if (completed != null) {
            boolean isCompleted = Boolean.parseBoolean(completed);
            reminders = reminderService.getRemindersByFamily(familyId, startDate, endDate)
                    .stream()
                    .filter(r -> r.isCompleted() == isCompleted)
                    .collect(Collectors.toList());
        } else {
            reminders = reminderService.getRemindersByFamily(familyId, startDate, endDate);
        }

        sendJsonResponse(resp, HttpServletResponse.SC_OK, reminders);
    }

    private void handleSingleReminder(int familyId, int reminderId, HttpServletResponse resp) throws Exception {
        Reminder reminder = reminderService.getReminderById(reminderId, familyId, null, null);

        if (reminder == null || reminder.getFamilyId() != familyId) {
            throw new NotFoundException("Reminder not found in this family");
        }

        sendJsonResponse(resp, HttpServletResponse.SC_OK, reminder);
    }

    private void validateReminder(Reminder reminder) {
        if (reminder.getTitle() == null || reminder.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (reminder.getDueDate() == null || reminder.getDueDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date must be in the future or present");
        }
        if (reminder.getType() == null || !reminder.getType().matches("^(payment|event|other)$")) {
            throw new IllegalArgumentException("Type must be payment, event or other");
        }
    }

    private void sendJsonResponse(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), data);
    }

    private void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), new ErrorResponse(message));
    }

    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}