import db.DBConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        System.out.println("Testing DB Connection...");
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            
            if (rs.next()) {
                System.out.println("JDBC Connection Successful: SELECT 1 returned " + rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("JDBC Connection Failed!");
        }
    }
}
