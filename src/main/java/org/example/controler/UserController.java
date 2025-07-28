package org.example.controler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.example.model.User;
import org.example.services.UserService;
import org.example.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(io.javalin.Javalin app, UserService userService) {
        this.userService = userService;
        registerRoutes(app);
    }

    private void registerRoutes(io.javalin.Javalin app) {
        // Регистрация пользователя
        app.post("/api/auth/register", this::registerUser);

        // Аутентификация пользователя
        app.post("/api/auth/login", this::authenticateUser);

        // Получение информации о пользователе
        app.get("/api/users/{username}", this::getUserByUsername);
    }

    private void registerUser(Context ctx) {
        try {
            User user = JsonUtil.fromJson(ctx.body(), User.class);
            userService.register(user);
            ctx.status(HttpStatus.CREATED).json(Map.of(
                    "message", "User registered successfully",
                    "username", user.getUsername()
            ));
        } catch (RuntimeException e) {
            logger.error("Registration failed", e);
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error during registration", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of(
                    "error", "Registration failed due to server error"
            ));
        }
    }

    private void authenticateUser(Context ctx) {
        try {
            Map<String, String> credentials = JsonUtil.fromJson(ctx.body(), Map.class);
            String username = credentials.get("username");
            String password = credentials.get("password");

            User authenticatedUser = userService.authenticate(username, password);
            if (authenticatedUser != null) {
                // Не возвращаем пароль в ответе
                authenticatedUser.setPassword(null);
                ctx.json(authenticatedUser);
            } else {
                ctx.status(HttpStatus.UNAUTHORIZED).json(Map.of(
                        "error", "Invalid username or password"
                ));
            }
        } catch (Exception e) {
            logger.error("Authentication failed", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of(
                    "error", "Authentication failed due to server error"
            ));
        }
    }

    private void getUserByUsername(Context ctx) {
        try {
            String username = ctx.pathParam("username");
            User user = userService.getUserByUsername(username);

            if (user != null) {
                // Не возвращаем пароль в ответе
                user.setPassword(null);
                ctx.json(user);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json(Map.of(
                        "error", "User not found"
                ));
            }
        } catch (Exception e) {
            logger.error("Error getting user", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of(
                    "error", "Failed to get user information"
            ));
        }
    }

    // Вспомогательный метод для UserService (нужно добавить в UserService)
    public User getUserByUsername(String username) {
        try {
            logger.debug("Getting user by username: {}", username);
            return userService.getUserByUsername(username);
        } catch (Exception e) {
            logger.error("Error getting user by username: " + username, e);
            throw new RuntimeException("Failed to get user", e);
        }
    }
}