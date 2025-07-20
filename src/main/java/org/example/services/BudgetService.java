package org.example.services;

import org.example.dao.BudgetDao;
import org.example.dao.TransactionDao;
import org.example.model.Budget;
import org.example.model.Transaction;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetService {
    private BudgetDao budgetDao;
    private TransactionDao transactionDao;

    public BudgetService(BudgetDao budgetDao, TransactionDao transactionDao) {
        this.budgetDao = budgetDao;
        this.transactionDao = transactionDao;
    }

    public void setBudget(int familyId, int categoryId, BigDecimal limitAmount, String period) throws SQLException {
        Budget budget = new Budget(0, familyId, categoryId, limitAmount, period);
        budgetDao.addBudget(budget);
    }

    public Map<Integer, BigDecimal> getBudgetStatus(int familyId) throws SQLException {
        Map<Integer, BigDecimal> status = new HashMap<>();
        List<Budget> budgets = budgetDao.getBudgetsByFamily(familyId);

        for (Budget budget : budgets) {
            List<Transaction> expenses = transactionDao.getTransactionsByFamilyAndCategory(
                    familyId, budget.getCategoryId(), "expense", LocalDate.now().withDayOfMonth(1), LocalDate.now());

            BigDecimal totalExpenses = expenses.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            status.put(budget.getCategoryId(), budget.getLimitAmount().subtract(totalExpenses));
        }

        return status;
    }
}
