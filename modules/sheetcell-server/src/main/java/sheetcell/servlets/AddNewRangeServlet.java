package sheetcell.servlets;

import api.Engine;
import com.google.gson.Gson;
import impl.sheet.SheetData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sheetcell.utils.ServletUtils;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/addNewRange")
public class AddNewRangeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Engine engine = ServletUtils.getEngine(getServletContext());

        String topLeftCell = request.getParameter("topLeftCell");
        String bottomRightCell = request.getParameter("bottomRightCell");
        String rangeName = request.getParameter("rangeName");

        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        SheetData sheetData = gson.fromJson(reader, SheetData.class);

        if (topLeftCell == null || bottomRightCell == null || rangeName == null || sheetData == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing parameters: topLeftCell, bottomRightCell, rangeName, or sheetData.");
            return;
        }

        try {
            engine.addNewRange(topLeftCell, bottomRightCell, rangeName, sheetData);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Range added successfully.");

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to add range: " + e.getMessage());
        }
    }
}

