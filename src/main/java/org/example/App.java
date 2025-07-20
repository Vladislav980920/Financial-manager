package org.example;
import org.example.dao.*;
import org.example.model.*;
import org.example.services.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;


public class App {
    public static void main(String[] args) {
        try {
            // Инициализация DAO
            UserDao userDao = new UserDao();
            FamilyDao familyDao = new FamilyDao();
            CategoryDao categoryDao = new CategoryDao();
            TransactionDao transactionDao = new TransactionDao();

            // 1. Проверка/создание пользователя
            String username = "Vladislav";
            User user1 = userDao.getUserByUsername(username);

            if (user1 == null) {
                // Создаем нового пользователя, если не существует
                user1 = new User(0, username, "password123", "Vlad@example.com", "admin");
                userDao.addUser(user1);
                System.out.println("Новый пользователь создан: " + username);
            } else {
                System.out.println("Используем существующего пользователя: " + username);
            }

            // 2. Проверка/создание семьи
            String familyName = "Smith Family";
            Family family = familyDao.getFamilyByName(familyName);

            if (family == null) {
                family = new Family(0, familyName);
                familyDao.addFamily(family);
                System.out.println("Новая семья создана: " + familyName);
            } else {
                System.out.println("Используем существующую семью: " + familyName);
            }

            // 3. Добавляем пользователя в семью (если еще не добавлен)
            FamilyMemberDao familyMemberDao = new FamilyMemberDao();
            if (!familyMemberDao.isMemberExists(family.getId(), user1.getId())) {
                FamilyMember member = new FamilyMember(0, family.getId(), user1.getId(), "head");
                familyMemberDao.addMember(member);
                System.out.println("Пользователь добавлен в семью");
            }

            // 4. Создаем категории (если еще не существуют)
            Category incomeCategory = categoryDao.getCategoryByNameAndFamily("Salary", family.getId());
            if (incomeCategory == null) {
                incomeCategory = new Category(0, "Salary", "income", family.getId());
                categoryDao.addCategory(incomeCategory);
            }

            Category expenseCategory = categoryDao.getCategoryByNameAndFamily("Rent", family.getId());
            if (expenseCategory == null) {
                expenseCategory = new Category(0, "Rent", "expense", family.getId());
                categoryDao.addCategory(expenseCategory);
            }

            // 5. Теперь можно добавлять транзакции
            FinanceService financeService = new FinanceService(transactionDao);
            financeService.addIncome(family.getId(), incomeCategory.getId(),
                    new BigDecimal("5000.00"), "Salary", LocalDate.now(), user1.getId());
            financeService.addExpense(family.getId(), expenseCategory.getId(),
                    new BigDecimal("1500.00"), "Rent", LocalDate.now(), user1.getId());

            System.out.println("Транзакции успешно добавлены");

        } catch (SQLException e) {
            System.err.println("Ошибка базы данных: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}