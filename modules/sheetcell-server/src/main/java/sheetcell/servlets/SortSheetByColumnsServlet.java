package sheetcell.servlets;

import com.google.gson.Gson;
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
        EngineImpl engine = (EngineImpl)ServletUtils.getEngine(getServletContext());

        String topLeft = request.getParameter("topLeft");
        String bottomRight = request.getParameter("bottomRight");

        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(reader).getAsJsonObject();



        List<String> columnToSortBy = gson.fromJson(jsonObject.get("columnToSortBy"), List.class);
        SheetData sheetData = gson.fromJson(jsonObject.get("sheetData"), SheetData.class);

        if (topLeft == null || bottomRight == null || sheetData == null || columnToSortBy == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing parameters: topLeft, bottomRight, columnToSortBy, or sheetData.");
            return;
        }

        try {
            SheetDTO sortedSheetDTO =(SheetDTO) engine.getSortedSheetDTO(columnToSortBy, topLeft, bottomRight, sheetData);

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(sortedSheetDTO));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to sort sheet by columns: " + e.getMessage());
        }
    }
}
