package org.example.services;

import org.example.dao.UserDao;
import org.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao = new UserDao();

    public UserService(UserDao userDao) throws SQLException {
    }

    public User authenticate(String username, String password) {
        try {
            logger.info("Authenticating user: {}", username);
            User user = userDao.getUserByUsername(username);
            if (user != null && user.getPassword().equals(password)) {
                logger.info("User authenticated successfully: {}", username);
                return user;
            }
            logger.warn("Authentication failed for user: {}", username);
            return null;
        } catch (Exception e) {
            logger.error("Error authenticating user: " + username, e);
            throw new RuntimeException("Authentication failed", e);
        }
    }
    public User getUserByUsername(String username) {
        try {
            logger.debug("Getting user by username: {}", username);
            return userDao.getUserByUsername(username);
        } catch (Exception e) {
            logger.error("Error getting user by username: " + username, e);
            throw new RuntimeException("Failed to get user", e);
        }
    }

    public void register(User user) {
        try {
            logger.info("Registering new user: {}", user.getUsername());
            if (userDao.userExists(user.getUsername())) {
                String message = "Пользователь с таким именем уже существует";
                logger.warn(message + ": " + user.getUsername());
                throw new RuntimeException(message);
            }
            userDao.addUser(user);
            logger.info("User registered successfully: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Error registering user: " + user.getUsername(), e);
            throw new RuntimeException("Registration failed", e);
        }
    }
}