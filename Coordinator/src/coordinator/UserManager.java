    package coordinator;

    import commonlib.models.User;

    import java.sql.*;

    public class UserManager {

        private Connection conn;

        public UserManager() {
            try {
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/distributed_fs", "root", "");
            } catch (SQLException e) {
                System.out.println("error connect db " + e.getMessage());
            }
        }

        public void registerUser(User user) {
            String sql = "INSERT INTO users (username, password, role, department) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getRole());
                stmt.setString(4, user.getDepartment());
                stmt.executeUpdate();
                System.out.println("login successfully " + user.getUsername());
            } catch (SQLException e) {
                System.out.println("login error " + e.getMessage());
            }
        }

        public User getUserByUsername(String username) {
            String sql = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("department")
                    );
                }
            } catch (SQLException e) {
                System.out.println("error fetch user data " + e.getMessage());
            }
            return null;
        }
    }
