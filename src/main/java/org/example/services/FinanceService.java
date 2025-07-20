package org.example.services;

import org.example.dao.TransactionDao;
import org.example.model.Transaction;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class FinanceService {
    private TransactionDao transactionDao;

    public FinanceService(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public void addIncome(int familyId, int categoryId, BigDecimal amount, String description, LocalDate date, int userId) throws SQLException {
        Transaction transaction = new Transaction(0, familyId, categoryId, amount, "income", description, date, userId);
        transactionDao.addTransaction(transaction);
    }

    public void addExpense(int familyId, int categoryId, BigDecimal amount, String description, LocalDate date, int userId) throws SQLException {
        Transaction transaction = new Transaction(0, familyId, categoryId, amount, "expense", description, date, userId);
        transactionDao.addTransaction(transaction);
    }

    public List<Transaction> getFamilyTransactions(int familyId) throws SQLException {
        return transactionDao.getTransactionsByFamily(familyId);
    }

    public BigDecimal calculateFamilyBalance(int familyId) throws SQLException {
        List<Transaction> transactions = transactionDao.getTransactionsByFamily(familyId);
        BigDecimal balance = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if ("income".equals(transaction.getType())) {
                balance = balance.add(transaction.getAmount());
            } else {
                balance = balance.subtract(transaction.getAmount());
            }
        }

        return balance;
    }
}
