package org.example.controler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.example.model.Family;
import org.example.services.FamilyService;
import org.example.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class FamilyController {
    private static final Logger logger = LoggerFactory.getLogger(FamilyController.class);
    private final FamilyService familyService;

    public FamilyController(io.javalin.Javalin app, FamilyService familyService) {
        this.familyService = familyService;
        registerRoutes(app);
    }

    private void registerRoutes(io.javalin.Javalin app) {
        // Создание семьи
        app.post("/api/families", this::createFamily);

        // Получение семьи по ID
        app.get("/api/families/{id}", this::getFamily);

        // Получение всех семей
        app.get("/api/families", this::getAllFamilies);

        // Обновление семьи
        app.put("/api/families/{id}", this::updateFamily);

        // Удаление семьи
        app.delete("/api/families/{id}", this::deleteFamily);

        // Поиск семей по названию
        app.get("/api/families/search", this::searchFamilies);

        // Получение количества членов семьи
        app.get("/api/families/{id}/members/count", this::getMembersCount);
    }

    private void createFamily(Context ctx) {
        try {
            Family family = JsonUtil.fromJson(ctx.body(), Family.class);
            Family createdFamily = familyService.addFamily(family);
            ctx.status(HttpStatus.CREATED).json(createdFamily);
        } catch (FamilyService.ServiceException e) {
            logger.error("Error creating family", e);
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating family", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", "Internal server error"));
        }
    }

    private void getFamily(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Family family = familyService.getFamilyById(id);

            if (family == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(Map.of("error", "Family not found"));
            } else {
                ctx.json(family);
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID format"));
        } catch (FamilyService.ServiceException e) {
            logger.error("Error getting family", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void getAllFamilies(Context ctx) {
        try {
            List<Family> families = familyService.getAllFamilies();
            ctx.json(families);
        } catch (FamilyService.ServiceException e) {
            logger.error("Error getting all families", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void updateFamily(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Family family = JsonUtil.fromJson(ctx.body(), Family.class);
            family.setId(id);

            Family updatedFamily = familyService.updateFamily(family);
            ctx.json(updatedFamily);
        } catch (FamilyService.NotFoundException e) {
            ctx.status(HttpStatus.NOT_FOUND).json(Map.of("error", e.getMessage()));
        } catch (FamilyService.ServiceException e) {
            logger.error("Error updating family", e);
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID format"));
        }
    }

    private void deleteFamily(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            familyService.deleteFamily(id);
            ctx.status(HttpStatus.NO_CONTENT);
        } catch (FamilyService.NotFoundException e) {
            ctx.status(HttpStatus.NOT_FOUND).json(Map.of("error", e.getMessage()));
        } catch (FamilyService.ServiceException e) {
            logger.error("Error deleting family", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID format"));
        }
    }

    private void searchFamilies(Context ctx) {
        try {
            String namePart = ctx.queryParam("name");
            if (namePart == null || namePart.isBlank()) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Name parameter is required"));
                return;
            }

            List<Family> families = familyService.searchFamiliesByName(namePart);
            ctx.json(families);
        } catch (FamilyService.ServiceException e) {
            logger.error("Error searching families", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void getMembersCount(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            int count = familyService.getMembersCount(id);
            ctx.json(Map.of("count", count));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID format"));
        } catch (FamilyService.ServiceException e) {
            logger.error("Error getting members count", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }
}