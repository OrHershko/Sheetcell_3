package sheetcell.servlets;

import com.google.gson.Gson;
import dto.SheetDTO;
import impl.sheet.SheetData;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sheetcell.utils.ServletUtils;

import java.io.IOException;

@WebServlet("/getSheetData")
public class GetSheetDataServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String sheetName = request.getParameter("sheetName");
            SheetData sheet = ServletUtils.getEngine(getServletContext()).getSheetData(sheetName);
            Gson gson = new Gson();
            String json = gson.toJson(sheet);
            response.setContentType("application/json");
            response.getWriter().write(json);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to retrieve sheet data");
        }
    }
}
