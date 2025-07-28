package org.example.util;

import org.example.dao.*;
import org.example.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    public static void init() {
        try {
            UserDao userDao = new UserDao();
            if (!userDao.userExists("admin")) {
                logger.info("Initializing database with default admin user");

                User admin = new User(0, "admin", "admin123", "admin@example.com", "admin");
                userDao.addUser(admin);
                logger.info("Created admin user with ID: {}", admin.getId());

                FamilyDao familyDao = new FamilyDao();
                Family family = new Family(0, "Административная семья");
                familyDao.addFamily(family);
                logger.info("Created admin family with ID: {}", family.getId());

                FamilyMemberDao memberDao = new FamilyMemberDao();
                FamilyMember member = new FamilyMember(0, family.getId(), admin.getId(), "head");
                memberDao.addMember(member);
                logger.info("Created family member association");

                // Создаем начальные категории
                CategoryDao categoryDao = new CategoryDao();
                createDefaultCategories(categoryDao, family.getId());
            }
        } catch (Exception e) {
            logger.error("Database initialization failed", e);
            throw new RuntimeException("Database initialization error", e);
        }
    }

    private static void createDefaultCategories(CategoryDao categoryDao, int familyId) {
        try {
            // Доходы
            categoryDao.addCategory(new Category(0, "Зарплата", "income", familyId));
            categoryDao.addCategory(new Category(0, "Инвестиции", "income", familyId));

            // Расходы
            categoryDao.addCategory(new Category(0, "Продукты", "expense", familyId));
            categoryDao.addCategory(new Category(0, "Коммунальные услуги", "expense", familyId));
            categoryDao.addCategory(new Category(0, "Транспорт", "expense", familyId));
            categoryDao.addCategory(new Category(0, "Развлечения", "expense", familyId));

            logger.info("Created default categories for family ID: {}", familyId);
        } catch (Exception e) {
            logger.error("Failed to create default categories", e);
        }
    }
}