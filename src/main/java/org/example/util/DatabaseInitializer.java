package org.example.util;

import org.example.dao.*;
import org.example.model.*;

public class DatabaseInitializer {
    public static void init() {
        try {
            // Создаем тестовые данные, если их нет
            UserDao userDao = new UserDao();
            if (!userDao.userExists("admin")) {
                User admin = new User(0, "admin", "admin123", "admin@example.com", "admin");
                userDao.addUser(admin);

                FamilyDao familyDao = new FamilyDao();
                Family family = new Family(0, "Административная семья");
                familyDao.addFamily(family);

                FamilyMemberDao memberDao = new FamilyMemberDao();
                memberDao.addMember(new FamilyMember(0, family.getId(), admin.getId(), "head"));

                // Инициализация категорий и т.д.
            }
        } catch (Exception e) {
            System.err.println("Ошибка инициализации БД: " + e.getMessage());
        }
    }
}
