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

import static impl.cell.Cell.getColumnFromCellID;
import static impl.cell.Cell.getRowFromCellID;

@WebServlet("/checkRangeOfCells")
public class CheckRangeOfCellsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // קבלת מנוע המערכת מה-context
        EngineImpl engine =(EngineImpl) ServletUtils.getEngine(getServletContext());

        // קבלת הפרמטרים topLeft ו-bottomRight
        String topLeft = request.getParameter("topLeft");
        String bottomRight = request.getParameter("bottomRight");

        // קריאת ה-SheetData מה-body של הבקשה
        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        SheetData sheetData = gson.fromJson(reader, SheetData.class);

        // בדיקה אם הפרמטרים סופקו
        if (topLeft == null || bottomRight == null || sheetData == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing topLeft, bottomRight or sheetData parameters.");
            return;
        }

        try {
            // בדיקה אם התאים בטווח
            boolean isInBounds = engine.isCellInBounds(getRowFromCellID(topLeft) - 1, getColumnFromCellID(topLeft) - 1, sheetData)
                    && engine.isCellInBounds(getRowFromCellID(bottomRight) - 1, getColumnFromCellID(bottomRight) - 1, sheetData);

            // החזרת התוצאה ללקוח
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(isInBounds));

        } catch (Exception e) {
            // טיפול בשגיאות במידה ויש
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to check range of cells: " + e.getMessage());
        }
    }
}