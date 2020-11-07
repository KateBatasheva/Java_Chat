package Server;

import java.sql.SQLException;

public class DBAuth implements Autherization {
    @Override
    public String getNick(String login, String password) throws SQLException {
        return DBManager.getNick(login,password);
    }

    @Override
    public boolean registr(String login, String password, String nick) throws SQLException {
        return DBManager.registr(login, password, nick);
    }

    @Override
    public boolean changeNick(String oldNick, String newNick) throws SQLException {
        return DBManager.changeNick(oldNick,newNick);
    }
}
