package org.example.services;

import org.example.dao.TransactionDao;
import org.example.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionDao transactionDao;

    public TransactionService(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
        logger.debug("TransactionService initialized");
    }

    public Transaction createTransaction(@Valid @NotNull Transaction transaction) {
        try {
            logger.info("Creating new transaction for familyId: {}, type: {}, amount: {}", transaction.getFamilyId(), transaction.getType(), transaction.getAmount());

            transactionDao.addTransaction(transaction);
            logger.info("Transaction created successfully with ID: {}", transaction.getId());

            return transaction;
        } catch (Exception e) {
            logger.error("Failed to create transaction for familyId: {}", transaction.getFamilyId(), e);
            throw new ServiceException("Failed to create transaction", e);
        }
    }

    public Transaction getTransactionById(@NotNull int id) {
        try {
            logger.debug("Retrieving transaction by ID: {}", id);
            Transaction transaction = transactionDao.getTransactionById(id);

            if (transaction == null) {
                logger.warn("Transaction not found for ID: {}", id);
            }

            return transaction;
        } catch (Exception e) {
            logger.error("Failed to retrieve transaction with ID: {}", id, e);
            throw new ServiceException("Failed to retrieve transaction", e);
        }
    }


    public List<Transaction> getFamilyTransactions(@NotNull int familyId) {
        try {
            logger.debug("Retrieving transactions for familyId: {}", familyId);
            List<Transaction> transactions = transactionDao.getTransactionsByFamily(familyId);
            logger.info("Found {} transactions for familyId: {}", transactions.size(), familyId);
            return transactions;
        } catch (Exception e) {
            logger.error("Failed to retrieve transactions for familyId: {}", familyId, e);
            throw new ServiceException("Failed to retrieve family transactions", e);
        }
    }

    public Transaction updateTransaction(@Valid @NotNull Transaction transaction) {
        try {
            logger.info("Updating transaction with ID: {}", transaction.getId());

            Transaction existing = transactionDao.getTransactionById(transaction.getId());
            if (existing == null) {
                logger.warn("Transaction not found for update, ID: {}", transaction.getId());
                throw new NotFoundException("Transaction not found");
            }

            transactionDao.updateTransaction(transaction);
            logger.info("Transaction updated successfully, ID: {}", transaction.getId());

            return transaction;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update transaction with ID: {}", transaction.getId(), e);
            throw new ServiceException("Failed to update transaction", e);
        }
    }


    public void deleteTransaction(@NotNull int id) {
        try {
            logger.info("Deleting transaction with ID: {}", id);

            Transaction existing = transactionDao.getTransactionById(id);
            if (existing == null) {
                logger.warn("Transaction not found for deletion, ID: {}", id);
                throw new NotFoundException("Transaction not found");
            }

            transactionDao.deleteTransaction(id);
            logger.info("Transaction deleted successfully, ID: {}", id);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete transaction with ID: {}", id, e);
            throw new ServiceException("Failed to delete transaction", e);
        }
    }

    public List<Transaction> getTransactionsByDateRange(@NotNull int familyId, @NotNull LocalDate startDate, @NotNull LocalDate endDate) {
        try {
            logger.debug("Retrieving transactions for familyId: {} between {} and {}", familyId, startDate, endDate);

            if (startDate.isAfter(endDate)) {
                logger.warn("Invalid date range: startDate {} is after endDate {}", startDate, endDate);
                throw new IllegalArgumentException("Start date must be before end date");
            }

            List<Transaction> transactions = transactionDao.getTransactionsByFamilyAndDateRange(familyId, startDate, endDate);
            logger.info("Found {} transactions for familyId: {} in date range", transactions.size(), familyId);

            return transactions;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to retrieve transactions by date range for familyId: {}", familyId, e);
            throw new ServiceException("Failed to retrieve transactions by date range", e);
        }
    }


    public BigDecimal calculateFamilyBalance(@NotNull int familyId) {
        try {
            logger.debug("Calculating balance for familyId: {}", familyId);

            List<Transaction> transactions = transactionDao.getTransactionsByFamily(familyId);
            BigDecimal balance = BigDecimal.ZERO;

            for (Transaction t : transactions) {
                if ("income".equals(t.getType())) {
                    balance = balance.add(t.getAmount());
                } else {
                    balance = balance.subtract(t.getAmount());
                }
            }

            logger.info("Calculated balance for familyId {}: {}", familyId, balance);
            return balance;
        } catch (Exception e) {
            logger.error("Failed to calculate balance for familyId: {}", familyId, e);
            throw new ServiceException("Failed to calculate family balance", e);
        }
    }


    public List<Transaction> getRecentTransactions(@NotNull int familyId, @NotNull int limit) {
        try {
            logger.debug("Retrieving {} recent transactions for familyId: {}", limit, familyId);

            if (limit <= 0) {
                logger.warn("Invalid limit value: {}", limit);
                throw new IllegalArgumentException("Limit must be positive");
            }

            List<Transaction> transactions = transactionDao.getRecentTransactions(familyId, limit);
            logger.info("Found {} recent transactions for familyId: {}", transactions.size(), familyId);

            return transactions;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to retrieve recent transactions for familyId: {}", familyId, e);
            throw new ServiceException("Failed to retrieve recent transactions", e);
        }
    }


    public static class ServiceException extends RuntimeException {
        public ServiceException(String message) {
            super(message);
        }

        public ServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}