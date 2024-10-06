package sheetcell.servlets;
import api.Engine;
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

@WebServlet("/getNumOfColumnsInGrid")
public class GetNumOfColumnsInGridServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // קבלת מנוע המערכת מה-context
        Engine engine = ServletUtils.getEngine(getServletContext());

        // קריאת ה-body של הבקשה (JSON)
        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        SheetData sheetData = gson.fromJson(reader, SheetData.class);

        // בדיקה אם הפרמטרים הנדרשים סופקו
        if (sheetData == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing sheetData parameter.");
            return;
        }

        try {
            // קבלת מספר העמודות בגיליון הנבחר
            int numOfColumns = engine.getNumOfColumnsInCurrSheet(sheetData);

            // שליחת מספר העמודות כתגובה ללקוח
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(numOfColumns));

        } catch (Exception e) {
            // טיפול בשגיאות אפשריות
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to get number of columns: " + e.getMessage());
        }
    }
}
