package sheetcell.servlets;

import com.google.gson.Gson;
import dto.SheetDTO;
import impl.sheet.SheetData;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sheetcell.utils.ServletUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/getSheetDTO")
public class GetSheetDTOServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // קריאת גוף הבקשה (JSON)
        BufferedReader reader = request.getReader();

        // המרת גוף הבקשה מ-JSON לאובייקט SheetData
        Gson gson = ServletUtils.getGson();
        SheetData sheetData = gson.fromJson(reader, SheetData.class);

        // קבלת ה-SheetDTO מהמערכת בהתבסס על ה-sheetName מ-SheetData
        SheetDTO sheetDTO = (SheetDTO) ServletUtils.getEngine(getServletContext()).getSheetDTO(sheetData);

        // המרת ה-SheetDTO ל-JSON והחזרתו כתגובה
        String jsonResponse = gson.toJson(sheetDTO);

        response.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.write(jsonResponse);
        }    }
}
