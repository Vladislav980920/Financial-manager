package org.example.services;

import org.example.dao.CategoryDao;
import org.example.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryDao categoryDao;

    public CategoryService(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
        logger.debug("CategoryService initialized");
    }

    // Добавление категории
    public void addCategory(Category category) {
        try {
            logger.info("Adding category: {}", category.getName());
            categoryDao.addCategory(category);
            logger.info("Category added successfully with ID: {}", category.getId());
        } catch (Exception e) {
            logger.error("Error adding category: " + category.getName(), e);
            throw new RuntimeException("Failed to add category", e);
        }
    }

    // Получение категории по ID
    public Category getCategoryById(int id) {
        try {
            logger.debug("Getting category by ID: {}", id);
            return categoryDao.getCategoryByNameAndFamily(null, id); // Предполагается, что метод будет дополнен в CategoryDao
        } catch (Exception e) {
            logger.error("Error getting category by ID: " + id, e);
            throw new RuntimeException("Failed to get category", e);
        }
    }

    // Получение всех категорий семьи
    public List<Category> getCategoriesByFamily(int familyId) {
        try {
            logger.debug("Getting categories for family ID: {}", familyId);
            return categoryDao.getCategoriesByFamily(familyId);
        } catch (Exception e) {
            logger.error("Error getting categories for family ID: " + familyId, e);
            throw new RuntimeException("Failed to get categories", e);
        }
    }

    // Получение категорий по типу (income/expense)
    public List<Category> getCategoriesByType(int familyId, String type) {
        try {
            logger.debug("Getting {} categories for family ID: {}", type, familyId);
            return categoryDao.getCategoriesByType(familyId, type);
        } catch (Exception e) {
            logger.error("Error getting {} categories for family ID: {}", type, familyId, e);
            throw new RuntimeException("Failed to get categories by type", e);
        }
    }

    // Обновление категории
    public void updateCategory(Category category) {
        try {
            logger.info("Updating category ID: {}", category.getId());
            categoryDao.updateCategory(category);
            logger.info("Category updated successfully");
        } catch (Exception e) {
            logger.error("Error updating category ID: " + category.getId(), e);
            throw new RuntimeException("Failed to update category", e);
        }
    }

    // Удаление категории
    public void deleteCategory(int id) {
        try {
            logger.info("Deleting category ID: {}", id);
            categoryDao.deleteCategory(id);
            logger.info("Category deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting category ID: " + id, e);
            throw new RuntimeException("Failed to delete category", e);
        }
    }

    // Проверка существования категории
    public boolean categoryExists(int id) {
        try {
            logger.debug("Checking if category exists ID: {}", id);
            return categoryDao.categoryExists(id);
        } catch (Exception e) {
            logger.error("Error checking category existence ID: " + id, e);
            throw new RuntimeException("Failed to check category existence", e);
        }
    }

    // Получение глобальных категорий (без привязки к семье)
    public List<Category> getGlobalCategories() {
        try {
            logger.debug("Getting global categories");
            return categoryDao.getGlobalCategories();
        } catch (Exception e) {
            logger.error("Error getting global categories", e);
            throw new RuntimeException("Failed to get global categories", e);
        }
    }

    // Поиск категорий по названию
    public List<Category> searchCategoriesByName(int familyId, String namePart) {
        try {
            logger.debug("Searching categories for family ID: {} with name: {}", familyId, namePart);
            return categoryDao.searchCategoriesByName(familyId, namePart);
        } catch (Exception e) {
            logger.error("Error searching categories for family ID: {} with name: {}", familyId, namePart, e);
            throw new RuntimeException("Failed to search categories", e);
        }
    }
}
