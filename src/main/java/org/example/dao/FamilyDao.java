package org.example.dao;

import org.example.model.Family;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FamilyDao {
    private Connection connection;

    public FamilyDao() {
        this.connection = DatabaseConnection.getConnection();
    }

    public void addFamily(Family family) throws SQLException {
        String sql = "INSERT INTO families (name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, family.getName());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    family.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public Family getFamilyByName(String name) throws SQLException {
        String sql = "SELECT * FROM families WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Family(
                            resultSet.getInt("id"),
                            resultSet.getString("name")
                    );
                }
            }
        }
        return null;
    }


    public List<Family> getAllFamilies() throws SQLException {
        List<Family> families = new ArrayList<>();
        String sql = "SELECT * FROM families";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                families.add(new Family(
                        resultSet.getInt("id"),
                        resultSet.getString("name")
                ));
            }
        }
        return families;
    }


    public void updateFamily(Family family) throws SQLException {
        String sql = "UPDATE families SET name = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, family.getName());
            statement.setInt(2, family.getId());

            statement.executeUpdate();
        }
    }

    public void deleteFamily(int id) throws SQLException {
        String sql = "DELETE FROM families WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public boolean familyExists(int id) throws SQLException {
        String sql = "SELECT 1 FROM families WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public List<Family> searchFamiliesByName(String namePart) throws SQLException {
        List<Family> families = new ArrayList<>();
        String sql = "SELECT * FROM families WHERE name LIKE ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + namePart + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    families.add(new Family(
                            resultSet.getInt("id"),
                            resultSet.getString("name")
                    ));
                }
            }
        }
        return families;
    }

    public int getFamilyMembersCount(int familyId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM family_members WHERE family_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }
}
