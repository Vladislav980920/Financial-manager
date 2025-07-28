package org.example.dao;

import lombok.SneakyThrows;
import org.example.model.Family;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FamilyDao {
    private Connection connection;

    @SneakyThrows
    public FamilyDao() {
        this.connection = DatabaseConnection.getConnection();
    }

    public void addFamily(Family family) {
        String sql = "INSERT INTO families (name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, family.getName());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    family.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding family", e);
        }
    }

    public Family getFamilyByName(String name) {
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
        } catch (SQLException e) {
            throw new RuntimeException("Error getting family by name", e);
        }
        return null;
    }

    public List<Family> getAllFamilies() {
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
        } catch (SQLException e) {
            throw new RuntimeException("Error getting all families", e);
        }
        return families;
    }

    public void updateFamily(Family family) {
        String sql = "UPDATE families SET name = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, family.getName());
            statement.setInt(2, family.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating family", e);
        }
    }

    public void deleteFamily(int id) {
        String sql = "DELETE FROM families WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting family", e);
        }
    }

    public boolean familyExists(int id) {
        String sql = "SELECT 1 FROM families WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if family exists", e);
        }
    }

    public List<Family> searchFamiliesByName(String namePart) {
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
        } catch (SQLException e) {
            throw new RuntimeException("Error searching families by name", e);
        }
        return families;
    }

    public int getFamilyMembersCount(int familyId) {
        String sql = "SELECT COUNT(*) FROM family_members WHERE family_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting family members count", e);
        }
        return 0;
    }
}