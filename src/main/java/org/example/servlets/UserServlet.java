package org.example.servlets;

import org.example.dao.UserDao;
import org.example.model.User;
import org.example.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/users/*")
public class UserServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserServlet.class);
    private UserService userService;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            userService = new UserService(new UserDao());
        } catch (Exception e) {
            throw new ServletException("Failed to initialize UserService", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetCurrentUser(req, resp);
            } else if (pathInfo.equals("/logout")) {
                handleLogout(req, resp);
            } else if (pathInfo.matches("/\\w+")) {
                String username = pathInfo.substring(1);
                User user = userService.getUserByUsername(username);
                if (user != null) {
                    sendJsonResponse(resp, user);
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                }
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleRegistration(req, resp);
            } else if (pathInfo.equals("/login")) {
                handleLogin(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    private void handleRegistration(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = parseUserFromRequest(req);
        userService.register(user);


        HttpSession session = req.getSession();
        session.setAttribute("user", user);
        session.setAttribute("username", user.getUsername());

        sendJsonResponse(resp, user, HttpServletResponse.SC_CREATED);
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        User user = userService.authenticate(username, password);
        if (user != null) {
            // Create session for authenticated user
            HttpSession session = req.getSession();
            session.setAttribute("user", user);
            session.setAttribute("username", user.getUsername());

            sendJsonResponse(resp, user);
        } else {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private void handleGetCurrentUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("username") != null) {
            String username = (String) session.getAttribute("username");
            User user = userService.getUserByUsername(username);
            if (user != null) {
                // Don't send password back to client
                user.setPassword(null);
                sendJsonResponse(resp, user);
                return;
            }
        }
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private User parseUserFromRequest(HttpServletRequest req) {

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String email = req.getParameter("email");
        String role = req.getParameter("role");

        if (username == null || password == null || email == null || role == null) {
            throw new IllegalArgumentException("Missing required user data");
        }

        return new User(0, username, password, email, role);
    }

    private void sendJsonResponse(HttpServletResponse resp, Object data) throws IOException {
        sendJsonResponse(resp, data, HttpServletResponse.SC_OK);
    }

    private void sendJsonResponse(HttpServletResponse resp, Object data, int status) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);


        String json = convertToJson(data);
        resp.getWriter().write(json);
    }

    private String convertToJson(Object data) {
        if (data instanceof User) {
            User user = (User) data;
            return String.format(
                    "{\"id\": %d, \"username\": \"%s\", \"email\": \"%s\", \"role\": \"%s\"}",
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole()
            );
        }
        return "{}";
    }

    private void handleException(HttpServletResponse resp, Exception e) throws IOException {
        logger.error("Error processing request", e);

        if (e instanceof IllegalArgumentException) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } else {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred");
        }
    }
}