package org.example.controler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.example.services.BudgetService;
import org.example.model.Budget;
import org.example.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;

public class BudgetController {
    private static final Logger logger = LoggerFactory.getLogger(BudgetController.class);
    private final BudgetService budgetService;

    public BudgetController(io.javalin.Javalin app, BudgetService budgetService) {
        this.budgetService = budgetService;
        registerRoutes(app);
    }

    private void registerRoutes(io.javalin.Javalin app) {
        // Установка бюджета
        app.post("/api/budgets", this::setBudget);

        // Получение статуса бюджета для семьи
        app.get("/api/budgets/status/{familyId}", this::getBudgetStatus);

        // Дополнительные маршруты можно добавить по аналогии
    }

    private void setBudget(Context ctx) {
        try {
            Budget budget = JsonUtil.fromJson(ctx.body(), Budget.class);
            budgetService.setBudget(
                    budget.getFamilyId(),
                    budget.getCategoryId(),
                    budget.getLimitAmount(),
                    budget.getPeriod()
            );
            ctx.status(HttpStatus.CREATED).result("Budget set successfully");
        } catch (Exception e) {
            logger.error("Error setting budget", e);
            ctx.status(HttpStatus.BAD_REQUEST).result("Failed to set budget: " + e.getMessage());
        }
    }

    private void getBudgetStatus(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            Map<Integer, BigDecimal> status = budgetService.getBudgetStatus(familyId);
            ctx.json(status);
        } catch (Exception e) {
            logger.error("Error getting budget status", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Failed to get budget status");
        }
    }
}