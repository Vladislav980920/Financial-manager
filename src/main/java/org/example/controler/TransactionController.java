package org.example.controler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.example.model.Transaction;
import org.example.services.TransactionService;
import org.example.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class TransactionController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;

    public TransactionController(io.javalin.Javalin app, TransactionService transactionService) {
        this.transactionService = transactionService;
        registerRoutes(app);
    }

    private void registerRoutes(io.javalin.Javalin app) {
        // Создание транзакции
        app.post("/api/families/{familyId}/transactions", this::createTransaction);

        // Получение транзакции по ID
        app.get("/api/transactions/{id}", this::getTransactionById);

        // Получение всех транзакций семьи
        app.get("/api/families/{familyId}/transactions", this::getFamilyTransactions);

        // Обновление транзакции
        app.put("/api/transactions/{id}", this::updateTransaction);

        // Удаление транзакции
        app.delete("/api/transactions/{id}", this::deleteTransaction);

        // Получение транзакций по диапазону дат
        app.get("/api/families/{familyId}/transactions/range", this::getTransactionsByDateRange);

        // Получение баланса семьи
        app.get("/api/families/{familyId}/balance", this::getFamilyBalance);

        // Получение последних транзакций
        app.get("/api/families/{familyId}/transactions/recent/{limit}", this::getRecentTransactions);
    }

    private void createTransaction(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            Transaction transaction = JsonUtil.fromJson(ctx.body(), Transaction.class);
            transaction.setFamilyId(familyId);

            Transaction createdTransaction = transactionService.createTransaction(transaction);
            ctx.status(HttpStatus.CREATED).json(createdTransaction);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid family ID format"));
        } catch (TransactionService.ServiceException e) {
            logger.error("Error creating transaction", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void getTransactionById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Transaction transaction = transactionService.getTransactionById(id);

            if (transaction == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(Map.of("error", "Transaction not found"));
            } else {
                ctx.json(transaction);
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid transaction ID format"));
        } catch (TransactionService.ServiceException e) {
            logger.error("Error getting transaction", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void getFamilyTransactions(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            List<Transaction> transactions = transactionService.getFamilyTransactions(familyId);
            ctx.json(transactions);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid family ID format"));
        } catch (TransactionService.ServiceException e) {
            logger.error("Error getting family transactions", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void updateTransaction(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Transaction transaction = JsonUtil.fromJson(ctx.body(), Transaction.class);
            transaction.setId(id);

            Transaction updatedTransaction = transactionService.updateTransaction(transaction);
            ctx.json(updatedTransaction);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid transaction ID format"));
        } catch (TransactionService.NotFoundException e) {
            ctx.status(HttpStatus.NOT_FOUND).json(Map.of("error", e.getMessage()));
        } catch (TransactionService.ServiceException e) {
            logger.error("Error updating transaction", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void deleteTransaction(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            transactionService.deleteTransaction(id);
            ctx.json(Map.of("message", "Transaction deleted successfully"));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid transaction ID format"));
        } catch (TransactionService.NotFoundException e) {
            ctx.status(HttpStatus.NOT_FOUND).json(Map.of("error", e.getMessage()));
        } catch (TransactionService.ServiceException e) {
            logger.error("Error deleting transaction", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void getTransactionsByDateRange(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            LocalDate startDate = LocalDate.parse(ctx.queryParam("startDate"));
            LocalDate endDate = LocalDate.parse(ctx.queryParam("endDate"));

            List<Transaction> transactions = transactionService.getTransactionsByDateRange(familyId, startDate, endDate);
            ctx.json(transactions);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid family ID format"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
        } catch (TransactionService.ServiceException e) {
            logger.error("Error getting transactions by date range", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void getFamilyBalance(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            BigDecimal balance = transactionService.calculateFamilyBalance(familyId);
            ctx.json(Map.of("balance", balance));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid family ID format"));
        } catch (TransactionService.ServiceException e) {
            logger.error("Error calculating family balance", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void getRecentTransactions(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            int limit = Integer.parseInt(ctx.pathParam("limit"));

            List<Transaction> transactions = transactionService.getRecentTransactions(familyId, limit);
            ctx.json(transactions);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID or limit format"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
        } catch (TransactionService.ServiceException e) {
            logger.error("Error getting recent transactions", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }
}