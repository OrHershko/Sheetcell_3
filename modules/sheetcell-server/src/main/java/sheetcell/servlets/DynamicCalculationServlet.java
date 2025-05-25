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
        Engine engine = ServletUtils.getEngine(getServletContext());

        String cellId = request.getParameter("cellId");
        String orgValue = request.getParameter("orgValue");

        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        SheetData sheetData = gson.fromJson(reader, SheetData.class);

        if (cellId == null || orgValue == null || sheetData == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing cellId, orgValue, or sheetData.");
            return;
        }

        try {
            CellValue newCellValue = EngineImpl.convertStringToCellValue(orgValue);

            SheetDTO updatedSheet = (SheetDTO) engine.DynamicCalculationOnSheet(cellId, newCellValue, orgValue, sheetData);

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(updatedSheet));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to perform dynamic calculation: " + e.getMessage());
        }
    }
}
