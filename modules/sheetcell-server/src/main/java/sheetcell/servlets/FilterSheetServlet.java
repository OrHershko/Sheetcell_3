package sheetcell.servlets;

import api.Engine;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
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
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

@WebServlet("/filterSheet")
public class FilterSheetServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Engine engine = ServletUtils.getEngine(getServletContext());

        String topLeft = request.getParameter("topLeft");
        String bottomRight = request.getParameter("bottomRight");

        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(reader).getAsJsonObject();

        Type type = new TypeToken<Map<String, Set<String>>>(){}.getType();
        Map<String, Set<String>> colToSelectedValues = gson.fromJson(jsonObject.get("colToSelectedValues"), type);

        SheetData sheetData = gson.fromJson(jsonObject.get("sheetData"), SheetData.class);

        if (topLeft == null || bottomRight == null || colToSelectedValues == null || sheetData == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing parameters: topLeft, bottomRight, colToSelectedValues, or sheetData.");
            return;
        }

        try {
            SheetDTO filteredSheetDTO = (SheetDTO)engine.getFilteredSheetDTO(colToSelectedValues, topLeft, bottomRight, sheetData);

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(filteredSheetDTO));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to filter sheet: " + e.getMessage());
        }
    }
}
