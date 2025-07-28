package org.example.controler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.example.model.Category;
import org.example.services.CategoryService;
import org.example.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CategoryController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    private final CategoryService categoryService;

    public CategoryController(io.javalin.Javalin app, CategoryService categoryService) {
        this.categoryService = categoryService;
        registerRoutes(app);
    }

    private void registerRoutes(io.javalin.Javalin app) {
        app.post("/api/categories", this::addCategory);
        app.get("/api/categories/family/{familyId}", this::getCategoriesByFamily);
        app.get("/api/categories/type/{familyId}/{type}", this::getCategoriesByType);
        app.put("/api/categories/{id}", this::updateCategory);
        app.delete("/api/categories/{id}", this::deleteCategory);
    }

    private void addCategory(Context ctx) {
        try {
            Category category = JsonUtil.fromJson(ctx.body(), Category.class);
            categoryService.addCategory(category);
            ctx.status(HttpStatus.CREATED).json(category);
        } catch (Exception e) {
            logger.error("Error adding category", e);
            ctx.status(HttpStatus.BAD_REQUEST).result("Failed to add category: " + e.getMessage());
        }
    }

    private void getCategoriesByFamily(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            List<Category> categories = categoryService.getCategoriesByFamily(familyId);
            ctx.json(categories);
        } catch (Exception e) {
            logger.error("Error getting categories by family", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Failed to get categories");
        }
    }

    private void getCategoriesByType(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            String type = ctx.pathParam("type");
            List<Category> categories = categoryService.getCategoriesByType(familyId, type);
            ctx.json(categories);
        } catch (Exception e) {
            logger.error("Error getting categories by type", e);
            ctx.status(HttpStatus.BAD_REQUEST).result("Invalid category type");
        }
    }

    private void updateCategory(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Category category = JsonUtil.fromJson(ctx.body(), Category.class);
            category.setId(id);
            categoryService.updateCategory(category);
            ctx.status(HttpStatus.OK).json(category);
        } catch (Exception e) {
            logger.error("Error updating category", e);
            ctx.status(HttpStatus.BAD_REQUEST).result("Failed to update category");
        }
    }

    private void deleteCategory(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            categoryService.deleteCategory(id);
            ctx.status(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            logger.error("Error deleting category", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Failed to delete category");
        }
    }
}
