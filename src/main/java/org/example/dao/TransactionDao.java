package org.example.dao;

import org.example.model.Transaction;
import org.example.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao {
    private Connection connection;

    public TransactionDao() {
        this.connection = DatabaseConnection.getConnection();
    }


    public void addTransaction(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (family_id, category_id, amount, type, description, date, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, transaction.getFamilyId());
            statement.setInt(2, transaction.getCategoryId());
            statement.setBigDecimal(3, transaction.getAmount());
            statement.setString(4, transaction.getType());
            statement.setString(5, transaction.getDescription());
            statement.setDate(6, Date.valueOf(transaction.getDate()));
            statement.setInt(7, transaction.getUserId());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public Transaction getTransactionById(int id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Transaction(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("category_id"),
                            resultSet.getBigDecimal("amount"),
                            resultSet.getString("type"),
                            resultSet.getString("description"),
                            resultSet.getDate("date").toLocalDate(),
                            resultSet.getInt("user_id")
                    );
                }
            }
        }
        return null;
    }

    public List<Transaction> getTransactionsByFamily(int familyId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE family_id = ? ORDER BY date DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(new Transaction(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("category_id"),
                            resultSet.getBigDecimal("amount"),
                            resultSet.getString("type"),
                            resultSet.getString("description"),
                            resultSet.getDate("date").toLocalDate(),
                            resultSet.getInt("user_id")
                    ));
                }
            }
        }
        return transactions;
    }

    public void updateTransaction(Transaction transaction) throws SQLException {
        String sql = "UPDATE transactions SET family_id = ?, category_id = ?, amount = ?, " +
                "type = ?, description = ?, date = ?, user_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, transaction.getFamilyId());
            statement.setInt(2, transaction.getCategoryId());
            statement.setBigDecimal(3, transaction.getAmount());
            statement.setString(4, transaction.getType());
            statement.setString(5, transaction.getDescription());
            statement.setDate(6, Date.valueOf(transaction.getDate()));
            statement.setInt(7, transaction.getUserId());
            statement.setInt(8, transaction.getId());

            statement.executeUpdate();
        }
    }

    public void deleteTransaction(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public List<Transaction> getTransactionsByFamilyAndCategory(int familyId, int categoryId, String expense, LocalDate localDate, LocalDate now) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE family_id = ? AND category_id = ? ORDER BY date DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setInt(2, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(new Transaction(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("category_id"),
                            resultSet.getBigDecimal("amount"),
                            resultSet.getString("type"),
                            resultSet.getString("description"),
                            resultSet.getDate("date").toLocalDate(),
                            resultSet.getInt("user_id")
                    ));
                }
            }
        }
        return transactions;
    }

    public List<Transaction> getTransactionsByFamilyAndType(int familyId, String transactionType) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE family_id = ? AND type = ? ORDER BY date DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setString(2, transactionType);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(new Transaction(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("category_id"),
                            resultSet.getBigDecimal("amount"),
                            resultSet.getString("type"),
                            resultSet.getString("description"),
                            resultSet.getDate("date").toLocalDate(),
                            resultSet.getInt("user_id")
                    ));
                }
            }
        }
        return transactions;
    }

    public List<Transaction> getTransactionsByFamilyAndDateRange(int familyId, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE family_id = ? AND date BETWEEN ? AND ? ORDER BY date DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setDate(2, Date.valueOf(startDate));
            statement.setDate(3, Date.valueOf(endDate));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(new Transaction(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("category_id"),
                            resultSet.getBigDecimal("amount"),
                            resultSet.getString("type"),
                            resultSet.getString("description"),
                            resultSet.getDate("date").toLocalDate(),
                            resultSet.getInt("user_id")
                    ));
                }
            }
        }
        return transactions;
    }

    public List<Transaction> getTransactionsByFamilyAndCategoryAndTypeAndDateRange(
            int familyId, int categoryId, String transactionType, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE family_id = ? AND category_id = ? " +
                "AND type = ? AND date BETWEEN ? AND ? ORDER BY date DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setInt(2, categoryId);
            statement.setString(3, transactionType);
            statement.setDate(4, Date.valueOf(startDate));
            statement.setDate(5, Date.valueOf(endDate));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(new Transaction(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("category_id"),
                            resultSet.getBigDecimal("amount"),
                            resultSet.getString("type"),
                            resultSet.getString("description"),
                            resultSet.getDate("date").toLocalDate(),
                            resultSet.getInt("user_id")
                    ));
                }
            }
        }
        return transactions;
    }

    public BigDecimal getTransactionsSumByFamilyAndCategoryAndTypeAndDateRange(
            int familyId, int categoryId, String transactionType, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT SUM(amount) FROM transactions WHERE family_id = ? AND category_id = ? " +
                "AND type = ? AND date BETWEEN ? AND ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setInt(2, categoryId);
            statement.setString(3, transactionType);
            statement.setDate(4, Date.valueOf(startDate));
            statement.setDate(5, Date.valueOf(endDate));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal(1) != null ? resultSet.getBigDecimal(1) : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public List<Transaction> getRecentTransactions(int familyId, int limit) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE family_id = ? ORDER BY date DESC, id DESC LIMIT ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setInt(2, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(new Transaction(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("category_id"),
                            resultSet.getBigDecimal("amount"),
                            resultSet.getString("type"),
                            resultSet.getString("description"),
                            resultSet.getDate("date").toLocalDate(),
                            resultSet.getInt("user_id")
                    ));
                }
            }
        }
        return transactions;
    }
}
