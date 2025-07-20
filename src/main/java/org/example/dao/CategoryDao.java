package org.example.dao;

import org.example.model.Category;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    private final Connection connection;

    public CategoryDao() {
        this.connection = DatabaseConnection.getConnection();
    }

    /**
     * Добавляет новую категорию в базу данных
     * @param category объект категории для добавления
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public void addCategory(Category category) throws SQLException {
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
        }
    }


    public Category getCategoryByNameAndFamily(String name, int familyId) throws SQLException {
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
        }
        return null;
    }

    /**
     * Получает все категории для указанной семьи
     * @param familyId идентификатор семьи
     * @return список категорий семьи
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<Category> getCategoriesByFamily(int familyId) throws SQLException {
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
        }
        return categories;
    }

    /**
     * Получает категории по типу (доход/расход)
     * @param familyId идентификатор семьи
     * @param type тип категории ("income" или "expense")
     * @return список категорий указанного типа
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<Category> getCategoriesByType(int familyId, String type) throws SQLException {
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
        }
        return categories;
    }

    /**
     * Обновляет информацию о категории
     * @param category объект категории с обновленными данными
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public void updateCategory(Category category) throws SQLException {
        String sql = "UPDATE categories SET name = ?, type = ?, family_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getType());
            statement.setInt(3, category.getFamilyId());
            statement.setInt(4, category.getId());

            statement.executeUpdate();
        }
    }

    /**
     * Удаляет категорию по ID
     * @param id идентификатор категории для удаления
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public void deleteCategory(int id) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    /**
     * Проверяет существование категории
     * @param id идентификатор категории
     * @return true если категория существует, иначе false
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public boolean categoryExists(int id) throws SQLException {
        String sql = "SELECT 1 FROM categories WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Получает глобальные категории (для всех семей)
     * @return список глобальных категорий
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<Category> getGlobalCategories() throws SQLException {
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
        }
        return categories;
    }

    /**
     * Ищет категории по названию (частичное совпадение)
     * @param familyId идентификатор семьи
     * @param namePart часть названия для поиска
     * @return список найденных категорий
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<Category> searchCategoriesByName(int familyId, String namePart) throws SQLException {
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
        }
        return categories;
    }

    /**
     * Получает количество категорий для семьи
     * @param familyId идентификатор семьи
     * @return количество категорий
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public int getCategoriesCount(int familyId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categories WHERE family_id = ?";
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
