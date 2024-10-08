package sheetcell.servlets;

import api.Engine;
import api.CellValue;
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

@WebServlet("/updateCell")
public class UpdateCellServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve the engine instance from the servlet context
        Engine engine = ServletUtils.getEngine(getServletContext());

        // Retrieve query parameters for cellId and newValue
        String cellId = request.getParameter("cellId");
        String newValueStr = request.getParameter("newValue");
        String usernameOfUpdater = request.getParameter("username");

        // Parse the SheetData from the request body (JSON)
        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        SheetData sheetData = gson.fromJson(reader, SheetData.class);

        // Check that parameters are provided
        if (cellId == null || newValueStr == null || sheetData == null || usernameOfUpdater == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing cellId, newValue, or sheetData parameters.");
            return;
        }

        try {
            // Convert the newValue to a CellValue
            CellValue newCellValue = EngineImpl.convertStringToCellValue(newValueStr);

            // Update the cell in the engine
            engine.updateCellValue(cellId, newCellValue, newValueStr, sheetData, usernameOfUpdater);

            // Send success response
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Cell updated successfully.");

        } catch (Exception e) {
            // Handle exceptions (e.g., invalid value format or cell ID)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to update cell: " + e.getMessage());
        }
    }
}
