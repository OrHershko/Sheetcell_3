package sheetcell.servlets;

import api.Engine;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import dto.SheetDTO;
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
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

@WebServlet("/filterSheet")
public class FilterSheetServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // קבלת מנוע המערכת מה-context
        Engine engine = ServletUtils.getEngine(getServletContext());

        // קבלת query parameters עבור topLeft ו-bottomRight
        String topLeft = request.getParameter("topLeft");
        String bottomRight = request.getParameter("bottomRight");

        // קריאת ה-body של הבקשה (JSON)
        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(reader).getAsJsonObject();

        // קבלת colToSelectedValues מה-body
        Type type = new TypeToken<Map<String, Set<String>>>(){}.getType();
        Map<String, Set<String>> colToSelectedValues = gson.fromJson(jsonObject.get("colToSelectedValues"), type);

        // קבלת sheetData מה-body
        SheetData sheetData = gson.fromJson(jsonObject.get("sheetData"), SheetData.class);

        // בדיקה אם כל הפרמטרים סופקו
        if (topLeft == null || bottomRight == null || colToSelectedValues == null || sheetData == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing parameters: topLeft, bottomRight, colToSelectedValues, or sheetData.");
            return;
        }

        try {
            // קבלת הגיליון המפולטר
            SheetDTO filteredSheetDTO = (SheetDTO)engine.getFilteredSheetDTO(colToSelectedValues, topLeft, bottomRight, sheetData);

            // המרת הגיליון המפולטר ל-JSON ושליחתו ללקוח
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(filteredSheetDTO));

        } catch (Exception e) {
            // טיפול בשגיאות אפשריות
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to filter sheet: " + e.getMessage());
        }
    }
}
