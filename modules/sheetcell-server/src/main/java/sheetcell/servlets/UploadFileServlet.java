package sheetcell.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import sheetcell.utils.ServletUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

@MultipartConfig(fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 5 * 5)
@WebServlet("/uploadFile")
public class UploadFileServlet extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        String username = request.getParameter("username");
        Part filePart = request.getPart("file");

        if (filePart != null) {
            try (InputStream inputStream = filePart.getInputStream()) {
                ServletUtils.getEngine(getServletContext()).loadFile(inputStream, username);
                out.println("File uploaded and processed successfully.");
            }
            catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(e.getMessage());
            }
            catch (Exception e) {
                out.println("Failed to process XML: " + e.getMessage());
                e.printStackTrace(out);
            }
        } else {
            out.println("Failed to upload file.");
        }
    }
}
