package org.example.servlets;

import org.example.model.FamilyMember;
import org.example.services.FamilyMemberService;
import org.example.services.FamilyMemberService.ServiceException;
import org.example.services.FamilyMemberService.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/families/*/members")
public class FamilyMemberServlet extends HttpServlet {
    private FamilyMemberService familyMemberService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        this.familyMemberService = new FamilyMemberService(new org.example.dao.FamilyMemberDao());
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            String[] pathParts = pathInfo.split("/");

            if (pathParts.length == 3) {
                int familyId = Integer.parseInt(pathParts[1]);
                handleListMembers(familyId, req, resp);
            } else if (pathParts.length == 5 && pathParts[3].equals("members")) {
                int familyId = Integer.parseInt(pathParts[1]);
                int memberId = Integer.parseInt(pathParts[4]);
                handleSingleMember(familyId, memberId, resp);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (NotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            String[] pathParts = pathInfo.split("/");

            if (pathParts.length != 3) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
                return;
            }

            int familyId = Integer.parseInt(pathParts[1]);
            FamilyMember member = objectMapper.readValue(req.getInputStream(), FamilyMember.class);
            member.setFamilyId(familyId);
            validateMember(member);

            FamilyMember createdMember = familyMemberService.addMember(member);
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, createdMember);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create family member");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            String[] pathParts = pathInfo.split("/");

            if (pathParts.length != 5 || !pathParts[3].equals("members")) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
                return;
            }

            int familyId = Integer.parseInt(pathParts[1]);
            int memberId = Integer.parseInt(pathParts[4]);
            FamilyMember member = objectMapper.readValue(req.getInputStream(), FamilyMember.class);
            member.setId(memberId);
            member.setFamilyId(familyId);
            validateMember(member);

            FamilyMember updatedMember = familyMemberService.updateMember(member);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, updatedMember);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update family member");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            String[] pathParts = pathInfo.split("/");

            if (pathParts.length != 5 || !pathParts[3].equals("members")) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
                return;
            }

            int familyId = Integer.parseInt(pathParts[1]);
            int memberId = Integer.parseInt(pathParts[4]);

            FamilyMember member = familyMemberService.getMemberById(memberId);
            if (member == null || member.getFamilyId() != familyId) {
                throw new NotFoundException("Family member not found in this family");
            }

            familyMemberService.deleteMember(memberId);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (NotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete family member");
        }
    }

    private void handleListMembers(int familyId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String roleParam = req.getParameter("role");

        try {
            List<FamilyMember> members;
            if (roleParam != null && !roleParam.trim().isEmpty()) {
                members = familyMemberService.getMembersByRole(familyId, roleParam);
            } else {
                members = familyMemberService.getMembersByFamily(familyId);
            }
            sendJsonResponse(resp, HttpServletResponse.SC_OK, members);
        } catch (Exception e) {
            throw new ServiceException("Failed to get family members", e);
        }
    }

    private void handleSingleMember(int familyId, int memberId, HttpServletResponse resp) throws IOException {
        try {
            FamilyMember member = familyMemberService.getMemberById(memberId);

            if (member == null || member.getFamilyId() != familyId) {
                throw new NotFoundException("Family member not found in this family");
            }

            sendJsonResponse(resp, HttpServletResponse.SC_OK, member);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to get family member", e);
        }
    }

    private void validateMember(FamilyMember member) {
        if (member.getUserId() <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (member.getRole() == null || !member.getRole().matches("^(admin|member|child)$")) {
            throw new IllegalArgumentException("Role must be admin, member or child");
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