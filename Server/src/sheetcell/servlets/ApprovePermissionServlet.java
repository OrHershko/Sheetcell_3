package sheetcell.servlets;

import com.google.gson.Gson;
import impl.EngineImpl;
import impl.sheet.SheetData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sheetcell.utils.ServletUtils;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/approvePermission")
public class ApprovePermissionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EngineImpl engine = (EngineImpl) ServletUtils.getEngine(getServletContext());

        String permissionType = request.getParameter("permissionType");
        String username = request.getParameter("username");

        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        SheetData sheetData = gson.fromJson(reader, SheetData.class);

        if (permissionType == null || sheetData == null || username == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing parameters.");
            return;
        }

        try {
            engine.approvePermissionRequest(permissionType, username, sheetData);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Request approved successfully.");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to approve request: " + e.getMessage());
        }
    }
}
