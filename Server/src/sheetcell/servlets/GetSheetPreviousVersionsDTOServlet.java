package sheetcell.servlets;


import api.DTO;
import api.Engine;
import com.google.gson.Gson;
import dto.SheetDTO;
import impl.sheet.SheetData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sheetcell.utils.ServletUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;



@WebServlet("/getSheetsPreviousVersions")
public class GetSheetPreviousVersionsDTOServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, IOException {

        // קריאת SheetData מתוך ה-JSON שנשלח בבקשה
        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        SheetData sheetData = gson.fromJson(reader, SheetData.class);

        if (sheetData == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing sheetData parameter.");
            return;
        }

        try {
            // קבלת גרסאות קודמות עבור ה-Sheet
            Map<Integer, SheetDTO> previousVersions = convertDTOMapToSheetDTOMap(sheetData);

            // המרת התוצאה ל-JSON
            String jsonResponse = gson.toJson(previousVersions);

            // החזרת התוצאה ללקוח
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();
            out.write(jsonResponse);

        } catch (Exception e) {
            // טיפול בשגיאות
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to retrieve previous sheet versions: " + e.getMessage());
        }
    }

    private Map<Integer, SheetDTO> convertDTOMapToSheetDTOMap(SheetData sheetData) {
        Engine engine = ServletUtils.getEngine(getServletContext());
        Map<Integer, DTO> mapToConvert = engine.getSheetsPreviousVersionsDTO(sheetData);
        Map<Integer, SheetDTO> resMap = new HashMap<>();

        for (Map.Entry<Integer, DTO> entry : mapToConvert.entrySet()) {
            resMap.put(entry.getKey(), (SheetDTO) entry.getValue());
        }

        return resMap;
    }
}
