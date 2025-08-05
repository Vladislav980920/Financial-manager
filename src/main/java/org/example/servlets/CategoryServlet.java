package org.example.servlets;

import org.example.model.Category;
import org.example.services.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/categories/*")
public class CategoryServlet extends HttpServlet {
    private CategoryService categoryService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        this.categoryService = new CategoryService(new org.example.dao.CategoryDao());
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                handleListCategories(req, resp);
            } else {
                handleSingleCategory(pathInfo, resp);
            }
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Category category = objectMapper.readValue(req.getInputStream(), Category.class);
            validateCategory(category);

            categoryService.addCategory(category);
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, category);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create category");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String[] parts = req.getPathInfo().split("/");
            if (parts.length != 2) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
                return;
            }

            int id = Integer.parseInt(parts[1]);
            Category category = objectMapper.readValue(req.getInputStream(), Category.class);
            category.setId(id);
            validateCategory(category);

            categoryService.updateCategory(category);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, category);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update category");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String[] parts = req.getPathInfo().split("/");
            if (parts.length != 2) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
                return;
            }

            int id = Integer.parseInt(parts[1]);
            categoryService.deleteCategory(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete category");
        }
    }

    private void handleListCategories(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String familyIdParam = req.getParameter("familyId");
        String typeParam = req.getParameter("type");
        String searchParam = req.getParameter("search");

        try {
            if (familyIdParam == null && typeParam == null && searchParam == null) {
                List<Category> categories = categoryService.getGlobalCategories();
                sendJsonResponse(resp, HttpServletResponse.SC_OK, categories);
            } else if (familyIdParam != null) {
                int familyId = Integer.parseInt(familyIdParam);

                if (typeParam != null) {
                    List<Category> categories = categoryService.getCategoriesByType(familyId, typeParam);
                    sendJsonResponse(resp, HttpServletResponse.SC_OK, categories);
                } else if (searchParam != null) {
                    List<Category> categories = categoryService.searchCategoriesByName(familyId, searchParam);
                    sendJsonResponse(resp, HttpServletResponse.SC_OK, categories);
                } else {
                    List<Category> categories = categoryService.getCategoriesByFamily(familyId);
                    sendJsonResponse(resp, HttpServletResponse.SC_OK, categories);
                }
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing familyId parameter");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid familyId format");
        }
    }

    private void handleSingleCategory(String pathInfo, HttpServletResponse resp) throws IOException {
        String[] parts = pathInfo.split("/");
        if (parts.length != 2) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            return;
        }

        try {
            int id = Integer.parseInt(parts[1]);
            Category category = categoryService.getCategoryById(id);

            if (category != null) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, category);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Category not found");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        }
    }

    private void validateCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        if (category.getType() == null || (!category.getType().equals("income") && !category.getType().equals("expense"))) {
            throw new IllegalArgumentException("Category type must be either 'income' or 'expense'");
        }
    }

    private void sendJsonResponse(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), data);
    }

    private void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), new ErrorResponse(message));
    }

    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}