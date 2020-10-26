package Server;

import java.sql.*;

// 1. Добавить в сетевой чат авторизацию через базу данных SQLite.

public class DBManager {

    private static Connection connection;
    private static PreparedStatement prepAdd;
    private static PreparedStatement prepGetLog;
    private static PreparedStatement prepGetNick;
    private static PreparedStatement prepChangeNick;
    private static PreparedStatement prepDeleteUsers;

    public DBManager() {
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
        prepDeleteUsers = connection.prepareStatement("DELETE FROM users " +
                "where login not null;");

    }

    public static void deleteUsers () throws SQLException {
        prepDeleteUsers.executeUpdate();
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


    public static void setConnection()  {
       try {
           Class.forName("org.sqlite.JDBC");
           connection = DriverManager.getConnection("jdbc:sqlite:main.db");
       } catch (ClassNotFoundException| SQLException e) {
           e.printStackTrace();
       }
    }

    public static String getNick(String login, String password) throws SQLException {
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

    public static boolean registr(String login, String password, String nick) throws SQLException {
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

    // 2.*Добавить в сетевой чат возможность смены ника.
    public static boolean changeNick (String oldNick, String newNick) throws SQLException {
        if (oldNick.equals(newNick)){
            return false;
        }
        prepChangeNick.setString(1,newNick);
        prepChangeNick.setString(2,oldNick);
return true;
    }
}

