    package coordinator;

    import java.sql.*;
    import java.util.*;

    public class DepartmentFileManager {
        private final Connection connection;

        public DepartmentFileManager(Connection connection) {
            this.connection = connection;
        }

        public List<String> getFilesByDepartment(String department) throws SQLException {
            List<String> files = new ArrayList<>();
            String sql = "SELECT filename FROM department_files WHERE department = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, department);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    files.add(rs.getString("filename"));
                }
            }
            return files;
        }

        public void addFile(String department, String filename) throws SQLException {
            String sql = "INSERT INTO department_files (department, filename) VALUES (?, ?) ON DUPLICATE KEY UPDATE filename=filename";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, department);
                stmt.setString(2, filename);
                stmt.executeUpdate();
            }
        }

        public void removeFile(String department, String filename) throws SQLException {
            String sql = "DELETE FROM department_files WHERE department = ? AND filename = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, department);
                stmt.setString(2, filename);
                stmt.executeUpdate();
            }
        }

        public boolean fileExists(String department, String filename) throws SQLException {
            String sql = "SELECT COUNT(*) FROM department_files WHERE department = ? AND filename = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, department);
                stmt.setString(2, filename);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }

        public List<String> getAllDepartments() throws SQLException {
            String sql = "SELECT DISTINCT department FROM department_files";
            List<String> departments = new ArrayList<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    departments.add(rs.getString("department"));
                }
            }
            return departments;
        }

    }
