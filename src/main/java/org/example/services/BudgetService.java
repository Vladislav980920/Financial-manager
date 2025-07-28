package org.example.services;

import org.example.dao.BudgetDao;
import org.example.dao.TransactionDao;
import org.example.model.Budget;
import org.example.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetService {
    private static final Logger logger = LoggerFactory.getLogger(BudgetService.class);
    private final BudgetDao budgetDao;
    private final TransactionDao transactionDao;

    public BudgetService(BudgetDao budgetDao, TransactionDao transactionDao) {
        this.budgetDao = budgetDao;
        this.transactionDao = transactionDao;
        logger.debug("BudgetService initialized");
    }

    public void setBudget(int familyId, int categoryId, BigDecimal limitAmount, String period) {
        try {
            logger.info("Setting budget for familyId: {}, categoryId: {}, limit: {}, period: {}",
                    familyId, categoryId, limitAmount, period);
            Budget budget = new Budget(0, familyId, categoryId, limitAmount, period);
            budgetDao.addBudget(budget);
            logger.info("Budget set successfully");
        } catch (Exception e) {
            logger.error("Error setting budget for familyId: " + familyId, e);
            throw new RuntimeException("Failed to set budget", e);
        }
    }

    public Map<Integer, BigDecimal> getBudgetStatus(int familyId) {
        logger.debug("Getting budget status for familyId: {}", familyId);
        try {
            Map<Integer, BigDecimal> status = new HashMap<>();
            List<Budget> budgets = budgetDao.getBudgetsByFamily(familyId);
            logger.debug("Found {} budgets for familyId: {}", budgets.size(), familyId);

            for (Budget budget : budgets) {
                List<Transaction> expenses = transactionDao.getTransactionsByFamilyAndCategory(
                        familyId, budget.getCategoryId(), "expense", LocalDate.now().withDayOfMonth(1), LocalDate.now());

                BigDecimal totalExpenses = expenses.stream()
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                status.put(budget.getCategoryId(), budget.getLimitAmount().subtract(totalExpenses));
                logger.trace("Calculated status for categoryId {}: {}", budget.getCategoryId(),
                        budget.getLimitAmount().subtract(totalExpenses));
            }

            return status;
        } catch (Exception e) {
            logger.error("Error getting budget status for familyId: " + familyId, e);
            throw new RuntimeException("Failed to get budget status", e);
        }
    }
}