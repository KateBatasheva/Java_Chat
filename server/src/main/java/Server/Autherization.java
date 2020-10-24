package Server;

import java.sql.SQLException;

public interface Autherization {
    String getNick (String login, String password) throws SQLException;
    boolean registr (String login, String password, String nick) throws SQLException;
}
