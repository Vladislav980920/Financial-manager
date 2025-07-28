package org.example;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.controler.*;
import org.example.dao.*;
import org.example.services.*;
import org.example.util.DatabaseInitializer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class App extends HttpServlet {
    private Javalin javalin;

    @Override
    public void init() throws ServletException {
        try {
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

            // Create Javalin app with basic config
            javalin = Javalin.create(config -> {
                config.jetty = "application/json";
                // Add any additional configuration here
            });

            // Initialize controllers
            new BudgetController(javalin, budgetService);
            new CategoryController(javalin, categoryService);
            new FamilyController(javalin, familyService);
            new GoalController(javalin, goalService);
            new ReminderController(javalin, reminderService);
            new UserController(javalin, userService);
            new TransactionController(javalin, transactionService);

            // Initialize database
            DatabaseInitializer.init();

        } catch (SQLException e) {
            throw new ServletException("Failed to initialize application", e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Javalin handles requests internally, so we don't need to implement this
        // Just forward to super implementation
        super.service(req, resp);
    }

    @Override
    public void destroy() {
        if (javalin != null) {
            javalin.stop();
        }
    }
}