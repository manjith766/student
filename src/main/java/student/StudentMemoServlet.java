package student;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;


public class StudentMemoServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String studentId = req.getParameter("id");
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        int total = 0, obtained = 0;
        boolean fail = false;
        boolean hasRecords = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/studentdb", "root", "root");

            PreparedStatement ps = con.prepareStatement("SELECT * FROM marks WHERE student_id = ?");
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            out.println("<h2>MemoCard for Student ID: " + studentId + "</h2>");
            out.println("<table border='1'><tr><th>Subject</th><th>Total</th><th>Obtained</th><th>Pass</th><th>Status</th></tr>");

            while (rs.next()) {
                hasRecords = true;
                String subject = rs.getString("subject");
                int t = rs.getInt("total_marks");
                int m = rs.getInt("marks_obtained");
                int p = rs.getInt("pass_mark");

                String status = (m >= p) ? "Pass" : "Fail";
                if (m < p) fail = true;

                total += t;
                obtained += m;

                out.println("<tr><td>" + subject + "</td><td>" + t + "</td><td>" + m + "</td><td>" + p + "</td><td>" + status + "</td></tr>");
            }

            out.println("</table>");

            if (hasRecords) {
                out.println("<p><strong>Total Marks:</strong> " + total + "</p>");
                out.println("<p><strong>Total Obtained:</strong> " + obtained + "</p>");
                out.println("<p><strong>Final Result:</strong> <b>" + (fail ? "Fail" : "Pass") + "</b></p>");
            } else {
                out.println("<p style='color:red;'>No records found for Student ID: " + studentId + "</p>");
            }

            con.close();
        } catch (Exception e) {
            out.println("<p>Error: " + e.getMessage() + "</p>");
        }
    }
}
