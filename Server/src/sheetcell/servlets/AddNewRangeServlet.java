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
        // קבלת ה-engine מה-context
        Engine engine = ServletUtils.getEngine(getServletContext());

        // קבלת הפרמטרים מה-URL
        String topLeftCell = request.getParameter("topLeftCell");
        String bottomRightCell = request.getParameter("bottomRightCell");
        String rangeName = request.getParameter("rangeName");

        // קבלת SheetData מה-Body (JSON)
        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        SheetData sheetData = gson.fromJson(reader, SheetData.class);

        // בדיקת תקינות הפרמטרים
        if (topLeftCell == null || bottomRightCell == null || rangeName == null || sheetData == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing parameters: topLeftCell, bottomRightCell, rangeName, or sheetData.");
            return;
        }

        try {
            // הוספת הטווח במנוע (engine)
            engine.addNewRange(topLeftCell, bottomRightCell, rangeName, sheetData);

            // החזרת תגובה חיובית
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Range added successfully.");

        } catch (Exception e) {
            // טיפול בשגיאות
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to add range: " + e.getMessage());
        }
    }
}

