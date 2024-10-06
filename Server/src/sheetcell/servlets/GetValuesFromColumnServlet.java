package sheetcell.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import java.util.Set;

@WebServlet("/getValuesFromColumn")
public class GetValuesFromColumnServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // קבלת מנוע המערכת מה-context
        EngineImpl engine = (EngineImpl) ServletUtils.getEngine(getServletContext());

        // קבלת הפרמטרים column, topLeft ו-bottomRight
        String column = request.getParameter("column");
        String topLeft = request.getParameter("topLeft");
        String bottomRight = request.getParameter("bottomRight");

        // קריאת ה-body של הבקשה (JSON)
        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

        // המרת SheetData מ-JSON
        SheetData sheetData = gson.fromJson(jsonObject.get("sheetData"), SheetData.class);

        // בדיקה אם כל הפרמטרים סופקו
        if (column == null || topLeft == null || bottomRight == null || sheetData == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing parameters: column, topLeft, bottomRight, or sheetData.");
            return;
        }

        try {
            // קבלת הערכים מהעמודה הנתונה
            Set<String> values = engine.getValuesFromColumn(column, topLeft, bottomRight, sheetData);

            // המרת הערכים ל-JSON ושליחתם כלקוח
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(values));

        } catch (Exception e) {
            // טיפול בשגיאות אפשריות
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to retrieve values from column: " + e.getMessage());
        }
    }
}
