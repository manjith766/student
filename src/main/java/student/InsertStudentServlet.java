package student;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.context.WebApplicationContext;

import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;

public class InsertStudentServlet extends HttpServlet {

    private DataSource dataSource;

    @Override
    public void init() {
        WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        this.dataSource = (DataSource) context.getBean("dataSource");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);

        try {
            JSONObject obj = new JSONObject(sb.toString());
            String studentId = obj.getString("student_id");
            String name = obj.getString("name");
            JSONArray marksArray = obj.getJSONArray("marks");

            try (Connection con = dataSource.getConnection()) {
                PreparedStatement psStudent = con.prepareStatement("INSERT INTO students VALUES (?, ?)");
                psStudent.setString(1, studentId);
                psStudent.setString(2, name);
                psStudent.executeUpdate();

                PreparedStatement psMark = con.prepareStatement(
                        "INSERT INTO marks (student_id, subject, total_marks, marks_obtained, pass_mark) VALUES (?, ?, ?, ?, ?)");

                for (int i = 0; i < marksArray.length(); i++) {
                    JSONObject mark = marksArray.getJSONObject(i);
                    psMark.setString(1, studentId);
                    psMark.setString(2, mark.getString("subject"));
                    psMark.setInt(3, mark.getInt("total_marks"));
                    psMark.setInt(4, mark.getInt("marks_obtained"));
                    psMark.setInt(5, mark.getInt("pass_mark"));
                    psMark.addBatch();
                }

                int[] results = psMark.executeBatch();
                response.setContentType("application/json");
                response.getWriter().println("{\"message\":\"Inserted " + results.length + " marks for student " + studentId + "\"}");
            }
        } catch (Exception e) {
            response.setContentType("application/json");
            response.getWriter().println("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
