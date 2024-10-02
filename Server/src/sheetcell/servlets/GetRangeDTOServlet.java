package sheetcell.servlets;

import api.Engine;
import com.google.gson.Gson;
import dto.RangeDTO;
import impl.sheet.SheetData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sheetcell.utils.ServletUtils;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/getRangeDTO")
public class GetRangeDTOServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, IOException {
        // קבלת ה-engine מה-context
        Engine engine = ServletUtils.getEngine(getServletContext());

        // קבלת פרמטר ה-rangeName מה-URL
        String rangeName = request.getParameter("rangeName");

        // קבלת SheetData מגוף הבקשה (JSON)
        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        SheetData sheetData = gson.fromJson(reader, SheetData.class);

        // בדיקת תקינות הפרמטרים
        if (rangeName == null || sheetData == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing rangeName or sheetData parameters.");
            return;
        }

        try {
            // קבלת ה-RangeDTO מה-engine
            RangeDTO rangeDTO = (RangeDTO) engine.getRangeDTOFromSheet(rangeName, sheetData);

            // המרת ה-RangeDTO ל-JSON והחזרה ללקוח
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(rangeDTO));

        } catch (Exception e) {
            // טיפול בשגיאות (לדוגמה, אם הטווח לא נמצא)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to retrieve range: " + e.getMessage());
        }
    }
}
