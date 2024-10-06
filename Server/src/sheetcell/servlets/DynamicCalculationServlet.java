package sheetcell.servlets;

import api.Engine;
import api.CellValue;
import com.google.gson.Gson;
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

@WebServlet("/dynamicCalculation")
public class DynamicCalculationServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // קבלת מנוע המערכת מה-context
        Engine engine = ServletUtils.getEngine(getServletContext());

        // קבלת פרמטרים מה-query של ה-URL
        String cellId = request.getParameter("cellId");
        String orgValue = request.getParameter("orgValue");

        // קריאת ה-body של הבקשה (JSON)
        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        SheetData sheetData = gson.fromJson(reader, SheetData.class);

        // בדיקה אם כל הפרמטרים סופקו
        if (cellId == null || orgValue == null || sheetData == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing cellId, orgValue, or sheetData.");
            return;
        }

        try {
            // המרת orgValue ל-CellValue
            CellValue newCellValue = EngineImpl.convertStringToCellValue(orgValue);

            // ביצוע החישוב הדינמי על הגיליון
            SheetDTO updatedSheet = (SheetDTO) engine.DynamicCalculationOnSheet(cellId, newCellValue, orgValue, sheetData);

            // שליחת SheetDTO המחושב בחזרה ללקוח
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(updatedSheet));

        } catch (Exception e) {
            // טיפול בשגיאות
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to perform dynamic calculation: " + e.getMessage());
        }
    }
}
