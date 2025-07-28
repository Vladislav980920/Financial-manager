package org.example.controler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.example.model.FinancialGoal;
import org.example.services.GoalService;
import org.example.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class GoalController {
    private static final Logger logger = LoggerFactory.getLogger(GoalController.class);
    private final GoalService goalService;

    public GoalController(io.javalin.Javalin app, GoalService goalService) {
        this.goalService = goalService;
        registerRoutes(app);
    }

    private void registerRoutes(io.javalin.Javalin app) {
        // Создание финансовой цели
        app.post("/api/families/{familyId}/goals", this::createGoal);

        // Получение целей семьи
        app.get("/api/families/{familyId}/goals", this::getFamilyGoals);

        // Внесение средств в цель
        app.post("/api/goals/{goalId}/contribute", this::contributeToGoal);

        // Получение прогресса по цели
        app.get("/api/goals/{goalId}/progress", this::getGoalProgress);

        // Получение целей по приоритету
        app.get("/api/families/{familyId}/goals/priority/{priority}", this::getGoalsByPriority);

        // Получение ближайших целей (по дате)
        app.get("/api/families/{familyId}/goals/upcoming/{days}", this::getUpcomingGoals);

        // Получение выполненных целей
        app.get("/api/families/{familyId}/goals/completed", this::getCompletedGoals);

        // Получение общей суммы целей
        app.get("/api/families/{familyId}/goals/total-target", this::getTotalTargetAmount);

        // Получение общей накопленной суммы
        app.get("/api/families/{familyId}/goals/total-current", this::getTotalCurrentAmount);
    }

    private void createGoal(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            FinancialGoal goal = JsonUtil.fromJson(ctx.body(), FinancialGoal.class);
            goal.setFamilyId(familyId);

            goalService.createGoal(
                    familyId,
                    goal.getName(),
                    goal.getDescription(),
                    goal.getTargetAmount(),
                    goal.getTargetDate(),
                    goal.getPriority()
            );

            ctx.status(HttpStatus.CREATED).json(Map.of("message", "Goal created successfully"));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid family ID format"));
        } catch (Exception e) {
            logger.error("Error creating goal", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", "Failed to create goal"));
        }
    }

    private void getFamilyGoals(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            List<FinancialGoal> goals = goalService.getFamilyGoals(familyId, familyId);
            ctx.json(goals);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid family ID format"));
        } catch (Exception e) {
            logger.error("Error getting family goals", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", "Failed to get goals"));
        }
    }

    private void contributeToGoal(Context ctx) {
        try {
            int goalId = Integer.parseInt(ctx.pathParam("goalId"));
            Map<String, BigDecimal> request = JsonUtil.fromJson(ctx.body(), Map.class);
            BigDecimal amount = request.get("amount");

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Amount must be positive"));
                return;
            }

            goalService.contributeToGoal(goalId, amount);
            ctx.json(Map.of("message", "Contribution successful"));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid goal ID format"));
        } catch (Exception e) {
            logger.error("Error contributing to goal", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", "Failed to contribute"));
        }
    }

    private void getGoalProgress(Context ctx) {
        try {
            int goalId = Integer.parseInt(ctx.pathParam("goalId"));
            BigDecimal progress = goalService.calculateGoalProgress(goalId);
            ctx.json(Map.of("progress", progress));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid goal ID format"));
        } catch (Exception e) {
            logger.error("Error calculating goal progress", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", "Failed to calculate progress"));
        }
    }

    private void getGoalsByPriority(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            String priority = ctx.pathParam("priority");

            if (!List.of("high", "medium", "low").contains(priority)) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid priority value"));
                return;
            }

            List<FinancialGoal> goals = goalService.getFamilyGoals(familyId, familyId);
            ctx.json(goals);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid family ID format"));
        } catch (Exception e) {
            logger.error("Error getting goals by priority", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", "Failed to get goals"));
        }
    }

    private void getUpcomingGoals(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            int days = Integer.parseInt(ctx.pathParam("days"));

            List<FinancialGoal> goals = goalService.getFamilyGoals(familyId, familyId);
            ctx.json(goals);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID or days format"));
        } catch (Exception e) {
            logger.error("Error getting upcoming goals", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", "Failed to get goals"));
        }
    }

    private void getCompletedGoals(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            List<FinancialGoal> goals = goalService.getFamilyGoals(familyId, familyId);
            ctx.json(goals);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid family ID format"));
        } catch (Exception e) {
            logger.error("Error getting completed goals", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", "Failed to get goals"));
        }
    }

    private void getTotalTargetAmount(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            BigDecimal total = (BigDecimal) goalService.getFamilyGoals(familyId, familyId);
            ctx.json(Map.of("totalTargetAmount", total));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid family ID format"));
        } catch (Exception e) {
            logger.error("Error getting total target amount", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", "Failed to get total"));
        }
    }

    private void getTotalCurrentAmount(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            Class<?> total = goalService.getClass();
            ctx.json(Map.of("totalCurrentAmount", total));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid family ID format"));
        } catch (Exception e) {
            logger.error("Error getting total current amount", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", "Failed to get total"));
        }
    }
}