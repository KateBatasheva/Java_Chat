package Server;

import java.sql.*;

public class DBautheriszation implements Autherization{

    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement prepAdd;
    private static PreparedStatement prepGetLog;
    private static PreparedStatement prepGetNick;

    public DBautheriszation(){
        try {
            setConnection();
            System.out.println("DB is connected");
            prepSQL();

        } catch (Exception o) {
            o.printStackTrace();
        } finally {
            disconnect();
        }
    }

    private void prepSQL () throws SQLException {
        prepAdd = connection.prepareStatement("INSERT INTO users (login, password, nick) VALUES (?, ?, ?)");
        prepGetLog = connection.prepareStatement("SELECT login FROM users WHERE login = ?");
        prepGetNick = connection.prepareStatement("SELECT nick, password FROM users WHERE login = ?");
    }
    public void fill (String log, String pas, String nik) throws SQLException {
        prepAdd.setString(1,log);
        prepAdd.setString(2,pas);
        prepAdd.setString(3,nik);
        prepAdd.executeUpdate();
    }
    private void getValue (String log, String pas, String nik) throws SQLException {

    }

    private void disconnect() {
        try {
            statement.close();
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
        statement = connection.createStatement();
    }

    @Override
    public String getNick(String login, String password) throws SQLException {
        ResultSet resultSet = null;
        if (prepGetNick.setString(1,login);)
        prepGetNick.setString(1,login);
        resultSet = prepGetNick.executeQuery();
    }

    @Override
    public boolean registr(String login, String password, String nick) throws SQLException {
        ResultSet resultSet = null;
        prepGetLog.setString(1,login);
        resultSet = prepGetLog.executeQuery();
        if (resultSet.next()) {
            resultSet.close();
            return false;
        }
        try {
            fill(login, password, nick);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            resultSet.close();
        }
        return true;
    }

    private static void exSelect() throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT login FROM users WHERE login = ?;");
        while (rs.next()) {
            System.out.println(rs.getString(1));
        }
        rs.close();
    }
}
