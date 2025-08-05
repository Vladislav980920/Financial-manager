package org.example.servlets;

import org.example.model.FinancialGoal;
import org.example.services.GoalService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/api/families/*/goals")
public class FinancialGoalServlet extends HttpServlet {
    private GoalService goalService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        this.goalService = new GoalService(new org.example.dao.FinancialGoalDao());
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            String[] pathParts = pathInfo.split("/");

            if (pathParts.length == 3) {

                int familyId = Integer.parseInt(pathParts[1]);
                handleListGoals(familyId, req, resp);
            } else if (pathParts.length == 5 && pathParts[3].equals("goals")) {

                int familyId = Integer.parseInt(pathParts[1]);
                int goalId = Integer.parseInt(pathParts[4]);
                handleSingleGoal(familyId, goalId, resp);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
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
            FinancialGoal goal = objectMapper.readValue(req.getInputStream(), FinancialGoal.class);
            goal.setFamilyId(familyId);
            validateGoal(goal);

            goalService.createGoal(
                    goal.getFamilyId(),
                    goal.getName(),
                    goal.getDescription(),
                    goal.getTargetAmount(),
                    goal.getTargetDate(),
                    goal.getPriority()
            );

            resp.setStatus(HttpServletResponse.SC_CREATED);
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, goal);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create goal");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            String[] pathParts = pathInfo.split("/");

            if (pathParts.length != 5 || !pathParts[3].equals("goals")) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
                return;
            }

            int familyId = Integer.parseInt(pathParts[1]);
            int goalId = Integer.parseInt(pathParts[4]);
            FinancialGoal goal = objectMapper.readValue(req.getInputStream(), FinancialGoal.class);
            goal.setId(goalId);
            goal.setFamilyId(familyId);
            validateGoal(goal);

            goalService.createGoal(
                    goal.getFamilyId(),
                    goal.getName(),
                    goal.getDescription(),
                    goal.getTargetAmount(),
                    goal.getTargetDate(),
                    goal.getPriority()
            );

            sendJsonResponse(resp, HttpServletResponse.SC_OK, goal);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update goal");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            String[] pathParts = pathInfo.split("/");

            if (pathParts.length != 5 || !pathParts[3].equals("goals")) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
                return;
            }

            int familyId = Integer.parseInt(pathParts[1]);
            int goalId = Integer.parseInt(pathParts[4]);


            goalService.getFamilyGoals(goalId, familyId); // Проверка существования


            new org.example.dao.FinancialGoalDao().deleteGoal(goalId);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete goal");
        }
    }

    private void handleListGoals(int familyId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String priority = req.getParameter("priority");
        String upcomingDays = req.getParameter("upcomingDays");
        String completed = req.getParameter("completed");

        try {
            List<FinancialGoal> goals;

            if (priority != null) {
                goals = new org.example.dao.FinancialGoalDao().getGoalsByFamilyAndPriority(familyId, priority);
            } else if (upcomingDays != null) {
                int days = Integer.parseInt(upcomingDays);
                goals = new org.example.dao.FinancialGoalDao().getUpcomingGoals(familyId, days);
            } else if (completed != null && Boolean.parseBoolean(completed)) {
                goals = new org.example.dao.FinancialGoalDao().getCompletedGoals(familyId);
            } else {
                goals = goalService.getFamilyGoals(0, familyId);
            }

            sendJsonResponse(resp, HttpServletResponse.SC_OK, goals);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid upcomingDays format");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get goals", e);
        }
    }

    private void handleSingleGoal(int familyId, int goalId, HttpServletResponse resp) throws IOException {
        try {
            FinancialGoal goal = new org.example.dao.FinancialGoalDao().getGoalById(goalId);

            if (goal == null || goal.getFamilyId() != familyId) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Goal not found in this family");
                return;
            }

            GoalResponse response = new GoalResponse(
                    goal,
                    goalService.calculateGoalProgress(goalId)
            );

            sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get goal", e);
        }
    }

    private void validateGoal(FinancialGoal goal) {
        if (goal.getName() == null || goal.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Goal name cannot be empty");
        }
        if (goal.getTargetAmount() == null || goal.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Target amount must be positive");
        }
        if (goal.getTargetDate() == null || goal.getTargetDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Target date must be in the future");
        }
        if (goal.getPriority() == null || !goal.getPriority().matches("^(high|medium|low)$")) {
            throw new IllegalArgumentException("Priority must be high, medium or low");
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

    private static class GoalResponse {
        private final FinancialGoal goal;
        private final BigDecimal progress;

        public GoalResponse(FinancialGoal goal, BigDecimal progress) {
            this.goal = goal;
            this.progress = progress;
        }

        public FinancialGoal getGoal() {
            return goal;
        }

        public BigDecimal getProgress() {
            return progress;
        }
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