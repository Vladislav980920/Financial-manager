package org.example;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.example.controler.*;
import org.example.dao.*;
import org.example.services.*;
import org.example.util.DatabaseInitializer;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

import static jdk.internal.org.jline.utils.Colors.h;

public class App extends HttpServlet {
    private Javalin javalin;

    @Override
    public void init() throws ServletException {
        try {
            // Initialize database
            DatabaseInitializer.init();

            // Initialize DAOs
            BudgetDao budgetDao = new BudgetDao();
            CategoryDao categoryDao = new CategoryDao();
            FamilyDao familyDao = new FamilyDao();
            FamilyMemberDao familyMemberDao = new FamilyMemberDao();
            FinancialGoalDao goalDao = new FinancialGoalDao();
            ReminderDao reminderDao = new ReminderDao();
            TransactionDao transactionDao = new TransactionDao();
            UserDao userDao = new UserDao();

            // Initialize services
            BudgetService budgetService = new BudgetService(budgetDao, transactionDao);
            CategoryService categoryService = new CategoryService(categoryDao);
            FamilyService familyService = new FamilyService(familyDao, familyMemberDao);
            GoalService goalService = new GoalService(goalDao);
            UserService userService = new UserService(userDao);
            ReminderService reminderService = new ReminderService(reminderDao);
            TransactionService transactionService = new TransactionService(transactionDao);

            // Configure Javalin
            javalin = Javalin.create(config -> {
                config.plugins.enableDevLogging();
            });

            // Initialize controllers
            new BudgetController(javalin, budgetService);
            new CategoryController(javalin, categoryService);
            new FamilyController(javalin, familyService);
            new GoalController(javalin, goalService);
            new ReminderController(javalin, reminderService, LocalDate.now(), LocalDate.now().plusMonths(1));
            new UserController(javalin, userService);
            new TransactionController(javalin, transactionService);

            // Add health check endpoint
            javalin.get("/health", ctx -> ctx.result("Server is running"));

        } catch (SQLException e) {
            throw new ServletException("Failed to initialize application", e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Forward all requests to Javalin
        @NotNull String String;
        if (javalin != null) javalin.get(String ).service(req, resp);
        else {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Application not initialized");
        }
    }

    @Override
    public void destroy() {
        if (javalin != null) {
            javalin.stop();
        }
    }
}