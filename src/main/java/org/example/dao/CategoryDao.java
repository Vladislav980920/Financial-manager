package org.example.dao;

import lombok.SneakyThrows;
import org.example.model.Category;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    private final Connection connection;

    @SneakyThrows
    public CategoryDao() {
        this.connection = DatabaseConnection.getConnection();
    }

    public void addCategory(Category category) {
        String sql = "INSERT INTO categories (name, type, family_id) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getType());
            statement.setInt(3, category.getFamilyId());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding category", e);
        }
    }

    public Category getCategoryByNameAndFamily(String name, int familyId) {
        String sql = "SELECT * FROM categories WHERE name = ? AND family_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setInt(2, familyId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Category(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("type"),
                            resultSet.getInt("family_id")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting category by name and family", e);
        }
        return null;
    }

    public List<Category> getCategoriesByFamily(int familyId) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE family_id = ? ORDER BY name";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    categories.add(new Category(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("type"),
                            resultSet.getInt("family_id")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting categories by family", e);
        }
        return categories;
    }

    public List<Category> getCategoriesByType(int familyId, String type) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE family_id = ? AND type = ? ORDER BY name";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setString(2, type);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    categories.add(new Category(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("type"),
                            resultSet.getInt("family_id")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting categories by type", e);
        }
        return categories;
    }

    public void updateCategory(Category category) {
        String sql = "UPDATE categories SET name = ?, type = ?, family_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getType());
            statement.setInt(3, category.getFamilyId());
            statement.setInt(4, category.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating category", e);
        }
    }

    public void deleteCategory(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting category", e);
        }
    }

    public boolean categoryExists(int id) {
        String sql = "SELECT 1 FROM categories WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if category exists", e);
        }
    }

    public List<Category> getGlobalCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE family_id IS NULL ORDER BY name";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                categories.add(new Category(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("type"),
                        resultSet.getInt("family_id")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting global categories", e);
        }
        return categories;
    }

    public List<Category> searchCategoriesByName(int familyId, String namePart) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE family_id = ? AND name LIKE ? ORDER BY name";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setString(2, "%" + namePart + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    categories.add(new Category(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("type"),
                            resultSet.getInt("family_id")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching categories by name", e);
        }
        return categories;
    }

    public int getCategoriesCount(int familyId) {
        String sql = "SELECT COUNT(*) FROM categories WHERE family_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting categories count", e);
        }
        return 0;
    }
}