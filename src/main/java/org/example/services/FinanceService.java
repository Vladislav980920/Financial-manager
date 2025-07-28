package org.example.services;

import org.example.dao.TransactionDao;
import org.example.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FinanceService {
    private static final Logger logger = LoggerFactory.getLogger(FinanceService.class);
    private final TransactionDao transactionDao;

    public FinanceService(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
        logger.debug("FinanceService initialized");
    }

    public void addIncome(int familyId, int categoryId, BigDecimal amount, String description, LocalDate date, int userId) {
        try {
            logger.info("Adding income for familyId: {}, amount: {}, categoryId: {}",
                    familyId, amount, categoryId);
            Transaction transaction = new Transaction(0, familyId, categoryId, amount, "income", description, date, userId);
            transactionDao.addTransaction(transaction);
            logger.info("Income added successfully");
        } catch (Exception e) {
            logger.error("Error adding income for familyId: " + familyId, e);
            throw new RuntimeException("Failed to add income", e);
        }
    }

    public void addExpense(int familyId, int categoryId, BigDecimal amount, String description, LocalDate date, int userId) {
        try {
            logger.info("Adding expense for familyId: {}, amount: {}, categoryId: {}",
                    familyId, amount, categoryId);
            Transaction transaction = new Transaction(0, familyId, categoryId, amount, "expense", description, date, userId);
            transactionDao.addTransaction(transaction);
            logger.info("Expense added successfully");
        } catch (Exception e) {
            logger.error("Error adding expense for familyId: " + familyId, e);
            throw new RuntimeException("Failed to add expense", e);
        }
    }

    public List<Transaction> getFamilyTransactions(int familyId) {
        logger.debug("Getting transactions for familyId: {}", familyId);
        try {
            List<Transaction> transactions = transactionDao.getTransactionsByFamily(familyId);
            logger.debug("Found {} transactions for familyId: {}", transactions.size(), familyId);
            return transactions;
        } catch (Exception e) {
            logger.error("Error getting transactions for familyId: " + familyId, e);
            throw new RuntimeException("Failed to get transactions", e);
        }
    }

    public BigDecimal calculateFamilyBalance(int familyId) {
        logger.debug("Calculating balance for familyId: {}", familyId);
        try {
            List<Transaction> transactions = transactionDao.getTransactionsByFamily(familyId);
            BigDecimal balance = BigDecimal.ZERO;

            for (Transaction transaction : transactions) {
                if ("income".equals(transaction.getType())) {
                    balance = balance.add(transaction.getAmount());
                } else {
                    balance = balance.subtract(transaction.getAmount());
                }
            }

            logger.info("Calculated balance for familyId {}: {}", familyId, balance);
            return balance;
        } catch (Exception e) {
            logger.error("Error calculating balance for familyId: " + familyId, e);
            throw new RuntimeException("Failed to calculate balance", e);
        }
    }
}