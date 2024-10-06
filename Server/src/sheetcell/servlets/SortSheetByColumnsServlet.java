package sheetcell.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import impl.EngineImpl;
import impl.sheet.SheetData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sheetcell.utils.ServletUtils;
import dto.SheetDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet("/sortSheetByColumns")
public class SortSheetByColumnsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // קבלת מנוע המערכת מה-context
        EngineImpl engine = (EngineImpl)ServletUtils.getEngine(getServletContext());

        // קבלת הפרמטרים topLeft ו-bottomRight
        String topLeft = request.getParameter("topLeft");
        String bottomRight = request.getParameter("bottomRight");

        // קריאת ה-body של הבקשה (JSON)
        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(reader).getAsJsonObject();



        // קבלת רשימת העמודות מ-json והמרת SheetData מה-JSON
        List<String> columnToSortBy = gson.fromJson(jsonObject.get("columnToSortBy"), List.class);
        SheetData sheetData = gson.fromJson(jsonObject.get("sheetData"), SheetData.class);

        // בדיקה אם הפרמטרים סופקו
        if (topLeft == null || bottomRight == null || sheetData == null || columnToSortBy == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing parameters: topLeft, bottomRight, columnToSortBy, or sheetData.");
            return;
        }

        try {
            // קריאה למנוע לקבלת הגיליון הממויין
            SheetDTO sortedSheetDTO =(SheetDTO) engine.getSortedSheetDTO(columnToSortBy, topLeft, bottomRight, sheetData);

            // המרת הגיליון הממויין ל-JSON ושליחתו כלקוח
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(sortedSheetDTO));

        } catch (Exception e) {
            // טיפול בשגיאות אפשריות
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to sort sheet by columns: " + e.getMessage());
        }
    }
}
