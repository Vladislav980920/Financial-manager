package org.example.servlets;

import org.example.model.Family;
import org.example.services.FamilyService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/views/dashboard")
public class DashboardServlet extends HttpServlet {
    private FamilyService familyService;

    @Override
    public void init() {
        this.familyService = new FamilyService(new org.example.dao.FamilyDao(),
                new org.example.dao.FamilyMemberDao());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect("/views/auth/login");
            return;
        }

        try {
            List<Family> families = familyService.getAllFamilies();
            req.setAttribute("families", families);
            req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException("Failed to load dashboard", e);
        }
    }
}
