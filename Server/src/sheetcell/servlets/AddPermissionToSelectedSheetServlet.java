package sheetcell.servlets;

import api.Engine;
import com.google.gson.Gson;
import dto.RangeDTO;
import impl.sheet.SheetData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sheetcell.utils.ServletUtils;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/addPermissionToSelectedSheet")
public class AddPermissionToSelectedSheetServlet extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, IOException {
        // קבלת ה-engine מה-context
        Engine engine = ServletUtils.getEngine(getServletContext());

        // קבלת פרמטר ה-rangeName מה-URL
        String requestType = request.getParameter("requestType");
        String usernameOfRequester = request.getParameter("username");

        // קבלת SheetData מגוף הבקשה (JSON)
        BufferedReader reader = request.getReader();
        Gson gson = ServletUtils.getGson();
        SheetData sheetData = gson.fromJson(reader, SheetData.class);

        // בדיקת תקינות הפרמטרים
        if (requestType == null || sheetData == null || usernameOfRequester == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing requestType or sheetData parameters.");
            return;
        }

        try {
            engine.addPermissionToSelectedSheet(sheetData, requestType, usernameOfRequester);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
