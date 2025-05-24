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

@WebServlet("/deleteRangeFromSheet")
public class DeleteRangeFromSheetServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EngineImpl engine = (EngineImpl)ServletUtils.getEngine(getServletContext());

        String rangeName = request.getParameter("rangeName");

        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        SheetData sheetData = gson.fromJson(reader, SheetData.class);

        if (rangeName == null || sheetData == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing rangeName or sheetData parameters.");
            return;
        }

        try {
            engine.deleteRangeFromSheet(rangeName, sheetData);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Range deleted successfully.");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to delete range: " + e.getMessage());
        }
    }
}
