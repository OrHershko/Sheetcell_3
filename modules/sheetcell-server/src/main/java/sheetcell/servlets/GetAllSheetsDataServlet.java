package sheetcell.servlets;

import com.google.gson.Gson;
import impl.sheet.SheetData;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sheetcell.utils.ServletUtils;
import java.io.IOException;
import java.util.List;

@WebServlet("/getAllSheetsData")
public class GetAllSheetsDataServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<SheetData> sheetList = ServletUtils.getEngine(getServletContext()).getAllSheetsData(request.getParameter("username"));

        Gson gson = new Gson();
        String json = gson.toJson(sheetList);

        response.setContentType("application/json");
        response.getWriter().write(json);
    }
}
