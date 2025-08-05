package org.example.servlets;

import org.example.model.Family;
import org.example.services.FamilyService;
import org.example.services.FamilyService.ServiceException;
import org.example.services.FamilyService.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/families/*")
public class FamilyServlet extends HttpServlet {
    private FamilyService familyService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {

        this.familyService = new FamilyService(
                new org.example.dao.FamilyDao(),
                new org.example.dao.FamilyMemberDao()
        );
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                handleListFamilies(req, resp);
            } else {
                handleSingleFamily(pathInfo, resp);
            }
        } catch (NotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Family family = objectMapper.readValue(req.getInputStream(), Family.class);
            validateFamily(family);

            Family createdFamily = familyService.addFamily(family);
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, createdFamily);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create family");
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
            Family family = objectMapper.readValue(req.getInputStream(), Family.class);
            family.setId(id);
            validateFamily(family);

            Family updatedFamily = familyService.updateFamily(family);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, updatedFamily);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update family");
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
            familyService.deleteFamily(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (NotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete family");
        }
    }

    private void handleListFamilies(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String searchParam = req.getParameter("search");

        try {
            List<Family> families;
            if (searchParam != null && !searchParam.trim().isEmpty()) {
                families = familyService.searchFamiliesByName(searchParam);
            } else {
                families = familyService.getAllFamilies();
            }
            sendJsonResponse(resp, HttpServletResponse.SC_OK, families);
        } catch (Exception e) {
            throw new ServiceException("Failed to get families list", e);
        }
    }

    private void handleSingleFamily(String pathInfo, HttpServletResponse resp) throws IOException {
        String[] parts = pathInfo.split("/");
        if (parts.length != 2) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            return;
        }

        try {
            int id = Integer.parseInt(parts[1]);
            Family family = familyService.getFamilyById(id);

            if (family != null) {
                int membersCount = familyService.getMembersCount(id);
                FamilyResponse response = new FamilyResponse(family, membersCount);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Family not found");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        }
    }

    private void validateFamily(Family family) {
        if (family.getName() == null || family.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Family name cannot be empty");
        }
        if (family.getName().length() < 2 || family.getName().length() > 100) {
            throw new IllegalArgumentException("Family name must be between 2 and 100 characters");
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

    private static class FamilyResponse {
        private final Family family;
        private final int membersCount;

        public FamilyResponse(Family family, int membersCount) {
            this.family = family;
            this.membersCount = membersCount;
        }

        public Family getFamily() {
            return family;
        }

        public int getMembersCount() {
            return membersCount;
        }
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