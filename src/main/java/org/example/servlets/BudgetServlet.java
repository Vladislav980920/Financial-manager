package org.example.servlets;

import lombok.SneakyThrows;
import org.example.model.Budget;
import org.example.services.BudgetService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@WebServlet("/api/budgets/*")
public class BudgetServlet extends HttpServlet {
    private BudgetService budgetService;
    private ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public void init() throws ServletException {
        super.init();
        this.budgetService = new BudgetService(
                new org.example.dao.BudgetDao(),
                new org.example.dao.TransactionDao()
        );
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                int familyId = Integer.parseInt(req.getParameter("familyId"));
                Map<Integer, BigDecimal> budgets = budgetService.getBudgetStatus(familyId);
                sendJsonResponse(resp, budgets);
            } else {
                String[] parts = pathInfo.split("/");
                if (parts.length == 2) {
                    int id = Integer.parseInt(parts[1]);
                    Budget budget = budgetService.setBudget();
                    if (budget != null) {
                        sendJsonResponse(resp, budget);
                    } else {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Budget not found");
                    }
                } else {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
                }
            }
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Budget budget = objectMapper.readValue(req.getInputStream(), Budget.class);
            budgetService.setBudget(
                    budget.getFamilyId(),
                    budget.getCategoryId(),
                    budget.getLimitAmount(),
                    budget.getPeriod()
            );
            resp.setStatus(HttpServletResponse.SC_CREATED);
            sendJsonResponse(resp, budget);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid budget data");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String[] parts = req.getPathInfo().split("/");
            if (parts.length == 2) {
                int id = Integer.parseInt(parts[1]);
                Budget budget = objectMapper.readValue(req.getInputStream(), Budget.class);
                budget.setId(id);
                budgetService.updateBudget(budget);
                sendJsonResponse(resp, budget);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            }
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String[] parts = req.getPathInfo().split("/");
            if (parts.length == 2) {
                int id = Integer.parseInt(parts[1]);
                budgetService.deleteBudget(id);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            }
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }

    private void sendJsonResponse(HttpServletResponse resp, Object data)
            throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(resp.getWriter(), data);
    }
}