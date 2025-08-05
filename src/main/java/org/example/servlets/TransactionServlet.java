package org.example.servlets;

import org.example.dao.TransactionDao;
import org.example.model.Transaction;
import org.example.services.TransactionService;
import org.example.services.TransactionService.ServiceException;
import org.example.services.TransactionService.NotFoundException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/transactions/*")
public class TransactionServlet extends HttpServlet {
    private TransactionService transactionService;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            transactionService = new TransactionService(new TransactionDao());
        } catch (Exception e) {
            throw new ServletException("Failed to initialize TransactionService", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                int familyId = Integer.parseInt(req.getParameter("familyId"));
                List<Transaction> transactions = transactionService.getFamilyTransactions(familyId);
                sendJsonResponse(resp, transactions);
            } else if (pathInfo.matches("/\\d+")) {
                int id = Integer.parseInt(pathInfo.substring(1));
                Transaction transaction = transactionService.getTransactionById(id);
                if (transaction != null) {
                    sendJsonResponse(resp, transaction);
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Transaction not found");
                }
            } else if (pathInfo.equals("/recent")) {
                int familyId = Integer.parseInt(req.getParameter("familyId"));
                int limit = Integer.parseInt(req.getParameter("limit"));
                List<Transaction> transactions = transactionService.getRecentTransactions(familyId, limit);
                sendJsonResponse(resp, transactions);
            } else if (pathInfo.equals("/balance")) {
                int familyId = Integer.parseInt(req.getParameter("familyId"));
                BigDecimal balance = transactionService.calculateFamilyBalance(familyId);
                sendJsonResponse(resp, balance);
            } else if (pathInfo.equals("/date-range")) {
                int familyId = Integer.parseInt(req.getParameter("familyId"));
                LocalDate startDate = LocalDate.parse(req.getParameter("startDate"));
                LocalDate endDate = LocalDate.parse(req.getParameter("endDate"));
                List<Transaction> transactions = transactionService.getTransactionsByDateRange(familyId, startDate, endDate);
                sendJsonResponse(resp, transactions);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
            }
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (DateTimeParseException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid date format");
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Transaction transaction = parseTransactionFromRequest(req);
            Transaction createdTransaction = transactionService.createTransaction(transaction);
            sendJsonResponse(resp, createdTransaction, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || !pathInfo.matches("/\\d+")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid transaction ID");
                return;
            }

            int id = Integer.parseInt(pathInfo.substring(1));
            Transaction transaction = parseTransactionFromRequest(req);
            transaction.setId(id);

            Transaction updatedTransaction = transactionService.updateTransaction(transaction);
            sendJsonResponse(resp, updatedTransaction);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || !pathInfo.matches("/\\d+")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid transaction ID");
                return;
            }

            int id = Integer.parseInt(pathInfo.substring(1));
            transactionService.deleteTransaction(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (ServiceException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Transaction parseTransactionFromRequest(HttpServletRequest req) throws IOException {
        try {
            int familyId = Integer.parseInt(req.getParameter("familyId"));
            int categoryId = Integer.parseInt(req.getParameter("categoryId"));
            BigDecimal amount = new BigDecimal(req.getParameter("amount"));
            String type = req.getParameter("type");
            String description = req.getParameter("description");
            LocalDate date = LocalDate.parse(req.getParameter("date"));
            int userId = Integer.parseInt(req.getParameter("userId"));

            return new Transaction(0, familyId, categoryId, amount, type, description, date, userId);
        } catch (NullPointerException | NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid transaction data", e);
        }
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
        if (data instanceof List) {
            return convertListToJson((List<?>) data);
        } else if (data instanceof Transaction) {
            return convertTransactionToJson((Transaction) data);
        } else if (data instanceof BigDecimal) {
            return "{\"balance\": " + data + "}";
        }
        return "{}";
    }

    private String convertTransactionToJson(Transaction transaction) {
        return String.format(
                "{\"id\": %d, \"familyId\": %d, \"categoryId\": %d, \"amount\": %s, \"type\": \"%s\", " +
                        "\"description\": \"%s\", \"date\": \"%s\", \"userId\": %d}",
                transaction.getId(),
                transaction.getFamilyId(),
                transaction.getCategoryId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getDescription(),
                transaction.getDate(),
                transaction.getUserId()
        );
    }

    private String convertListToJson(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        for (Object item : list) {
            if (item instanceof Transaction) {
                sb.append(convertTransactionToJson((Transaction) item));
            }
            sb.append(",");
        }
        if (!list.isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]");
        return sb.toString();
    }
}