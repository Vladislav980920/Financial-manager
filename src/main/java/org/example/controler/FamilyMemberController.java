package org.example.controler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.example.model.FamilyMember;
import org.example.services.FamilyMemberService;
import org.example.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class FamilyMemberController {
    private static final Logger logger = LoggerFactory.getLogger(FamilyMemberController.class);
    private final FamilyMemberService familyMemberService;

    public FamilyMemberController(io.javalin.Javalin app, FamilyMemberService familyMemberService) {
        this.familyMemberService = familyMemberService;
        registerRoutes(app);
    }

    private void registerRoutes(io.javalin.Javalin app) {
        // Добавление члена семьи
        app.post("/api/families/{familyId}/members", this::addMember);

        // Получение члена семьи по ID
        app.get("/api/families/members/{id}", this::getMember);

        // Получение всех членов семьи
        app.get("/api/families/{familyId}/members", this::getMembersByFamily);

        // Получение семей пользователя
        app.get("/api/users/{userId}/families", this::getFamiliesByUser);

        // Обновление члена семьи
        app.put("/api/families/members/{id}", this::updateMember);

        // Удаление члена семьи
        app.delete("/api/families/members/{id}", this::deleteMember);

        // Получение роли пользователя в семье
        app.get("/api/families/{familyId}/members/{userId}/role", this::getMemberRole);

        // Получение количества членов семьи
        app.get("/api/families/{familyId}/members/count", this::getMembersCount);

        // Получение членов семьи по роли
        app.get("/api/families/{familyId}/members/role/{role}", this::getMembersByRole);
    }

    private void addMember(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            FamilyMember member = JsonUtil.fromJson(ctx.body(), FamilyMember.class);
            member.setFamilyId(familyId);

            FamilyMember createdMember = familyMemberService.addMember(member);
            ctx.status(HttpStatus.CREATED).json(createdMember);
        } catch (FamilyMemberService.ServiceException e) {
            logger.error("Error adding family member", e);
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID format"));
        } catch (Exception e) {
            logger.error("Unexpected error adding family member", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", "Internal server error"));
        }
    }

    private void getMember(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            FamilyMember member = familyMemberService.getMemberById(id);

            if (member == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(Map.of("error", "Family member not found"));
            } else {
                ctx.json(member);
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID format"));
        } catch (FamilyMemberService.ServiceException e) {
            logger.error("Error getting family member", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void getMembersByFamily(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            List<FamilyMember> members = familyMemberService.getMembersByFamily(familyId);
            ctx.json(members);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID format"));
        } catch (FamilyMemberService.ServiceException e) {
            logger.error("Error getting family members", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void getFamiliesByUser(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            List<FamilyMember> memberships = familyMemberService.getFamiliesByUser(userId);
            ctx.json(memberships);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID format"));
        } catch (FamilyMemberService.ServiceException e) {
            logger.error("Error getting user's families", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void updateMember(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            FamilyMember member = JsonUtil.fromJson(ctx.body(), FamilyMember.class);
            member.setId(id);

            FamilyMember updatedMember = familyMemberService.updateMember(member);
            ctx.json(updatedMember);
        } catch (FamilyMemberService.NotFoundException e) {
            ctx.status(HttpStatus.NOT_FOUND).json(Map.of("error", e.getMessage()));
        } catch (FamilyMemberService.ServiceException e) {
            logger.error("Error updating family member", e);
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID format"));
        }
    }

    private void deleteMember(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            familyMemberService.deleteMember(id);
            ctx.status(HttpStatus.NO_CONTENT);
        } catch (FamilyMemberService.NotFoundException e) {
            ctx.status(HttpStatus.NOT_FOUND).json(Map.of("error", e.getMessage()));
        } catch (FamilyMemberService.ServiceException e) {
            logger.error("Error deleting family member", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID format"));
        }
    }

    private void getMemberRole(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            int userId = Integer.parseInt(ctx.pathParam("userId"));

            String role = familyMemberService.getMemberRole(familyId, userId);
            if (role == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(Map.of("error", "Member not found"));
            } else {
                ctx.json(Map.of("role", role));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID format"));
        } catch (FamilyMemberService.ServiceException e) {
            logger.error("Error getting member role", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void getMembersCount(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            int count = familyMemberService.getMembersCount(familyId);
            ctx.json(Map.of("count", count));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID format"));
        } catch (FamilyMemberService.ServiceException e) {
            logger.error("Error getting members count", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }

    private void getMembersByRole(Context ctx) {
        try {
            int familyId = Integer.parseInt(ctx.pathParam("familyId"));
            String role = ctx.pathParam("role");

            List<FamilyMember> members = familyMemberService.getMembersByRole(familyId, role);
            ctx.json(members);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Invalid ID format"));
        } catch (FamilyMemberService.ServiceException e) {
            logger.error("Error getting members by role", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", e.getMessage()));
        }
    }
}