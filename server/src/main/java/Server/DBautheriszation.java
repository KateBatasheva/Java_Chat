package Server;

import java.io.IOException;
import java.sql.*;

public class DBautheriszation implements Autherization {

    private static Connection connection;
    private static PreparedStatement prepAdd;
    private static PreparedStatement prepGetLog;
    private static PreparedStatement prepGetNick;
    private static PreparedStatement prepChangeNick;

    public DBautheriszation() {
        try {
            setConnection();
            System.out.println("DB is connected");
            prepSQL();
            System.out.println("PrepSQL is connected");

        } catch (Exception o) {
            o.printStackTrace();
        }
    }

    private void prepSQL() throws SQLException {
        prepAdd = connection.prepareStatement("INSERT INTO users (Login, Password, Nick) VALUES (?, ?, ?);");
        prepGetLog = connection.prepareStatement("SELECT Login FROM users WHERE Login = ?;");
        prepGetNick = connection.prepareStatement("SELECT * FROM users WHERE Login = ?;");
        prepChangeNick = connection.prepareStatement("UPDATE users SET Nick = ? WHERE Nick = ?;");

    }

    public void fill(String log, String pas, String nik) throws SQLException {
        prepAdd.setString(1, log);
        prepAdd.setString(2, pas);
        prepAdd.setString(3, nik);
        prepAdd.executeUpdate();
    }

    public static void disconnect() {
        try {
            prepAdd.close();
            prepGetNick.close();
            prepGetLog.close();
            prepChangeNick.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void setConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
    }

    @Override
    public String getNick(String login, String password) throws SQLException {
        ResultSet resultSet = null;
        try {
            prepGetNick.setString(1, login);
            resultSet = prepGetNick.executeQuery();
            if (resultSet.getString(2).equals(password)) {
                return resultSet.getString(3);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            resultSet.close();
        }
        return null;
    }

    @Override
    public boolean registr(String login, String password, String nick) throws SQLException {
        ResultSet resultSet = null;
        prepGetLog.setString(1, login);
        resultSet = prepGetLog.executeQuery();
        if (resultSet.next()) {
            prepGetLog.close();
            return false;
        }
        prepAdd.setString(1, login);
        prepAdd.setString(2, password);
        prepAdd.setString(3, nick);
        prepAdd.executeUpdate();
        resultSet.close();
        return true;
    }
    @Override
    public boolean changeNick (String oldNick, String newNick) throws SQLException {
        if (oldNick.equals(newNick)){
            return false;
        }
        prepChangeNick.setString(1,newNick);
        prepChangeNick.setString(2,oldNick);
return true;
    }
}

