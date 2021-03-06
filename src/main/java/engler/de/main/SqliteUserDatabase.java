package engler.de.main;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by johannes on 17.04.17.
 */
public class SqliteUserDatabase {
    private static final String JDBC = "jdbc:sqlite:";
    private String database;
    private final static String USERS ="users";
    private static SqliteUserDatabase instance = null;


    public SqliteUserDatabase(String database) {
        this.database = database;
        String createLines = "create table users (id integer, username string, passwordhash string, salt string, extension string)";
        createDatabase(database, createLines);
    }

    private void createDatabase(String database, String createLine) {
        File f = new File(database);
        if (f.exists() && !f.isDirectory()) {
            try (Connection connection = DriverManager.getConnection(JDBC + database)) {
                String query = "SELECT name FROM sqlite_master WHERE type=?";
                try (PreparedStatement ptsm = connection.prepareStatement(query)) {
                    ptsm.setString(1, "table");
                    ResultSet table = ptsm.executeQuery();
                    ArrayList<String> check = new ArrayList<>();
                    while (table.next()) {
                        Logger.getLogger(getClass().getName()).info(table.getString("name"));
                        check.add(table.getString("name"));
                    }
                    if (!(check.contains(USERS))) {
                        throw new SQLException("Database seems not to have the right scheme");
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            try (Connection connection = DriverManager.getConnection(JDBC + database)) {
                try (Statement statement = connection.createStatement()) {
                    // Erstelle die Datenbank für das Programm
                    statement.setQueryTimeout(30);
                    statement.executeUpdate(createLine);
                }
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
            }


        }

    }

    public List<String> getDatabaseEntries() {
        ArrayList<String> inputs = new ArrayList<>();
        String query = "Select username, extension from users";
        try (Connection connection = DriverManager.getConnection(JDBC + database); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setQueryTimeout(10);
            ResultSet rs  = statement.executeQuery();
            while (rs.next()) {
                inputs.add(rs.getString(1)+ "\t"+ rs.getString(2));
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return inputs;
    }

    public void updateStatementForHash(String username, String passwordhash, String salt) {
        try (Connection con = DriverManager.getConnection(JDBC + database); Statement statement = con.createStatement()) {
            statement.setQueryTimeout(10);
            final String query = "UPDATE users SET passwordhash='" + passwordhash + "', salt='" + salt + "' WHERE username='" + username + "'";
            statement.execute(query);
            statement.closeOnCompletion();
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, null, ex);
        }
    }

    public boolean insertUserInDatabase(String username, String passwordhash, String salt, String extension) {
        if (!isUserAlreadyInDatabase(username)) {
            try (Connection con = DriverManager.getConnection(JDBC + database); Statement statement = con.createStatement()) {
                statement.setQueryTimeout(10);
                final String query = "INSERT INTO users (id, username , passwordhash, salt, extension) " +
                        "Values (((Select max(id) from users)+1),'" + username + "','" + passwordhash + "' , '" + salt + "', '" + extension + "' )";
                statement.execute(query);
                statement.closeOnCompletion();
            } catch (SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, null, ex);
            }
            return true;
        } else {
            Logger.getLogger(getClass().getName()).info("Was already in database.");
            return false;
        }
    }




    public boolean isUserAlreadyInDatabase(String username) {
        String query = "Select * from users where username = ?";
        try (Connection connection = DriverManager.getConnection(JDBC + database); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1,username);
            ResultSet resultSet = statement.executeQuery();
            statement.setQueryTimeout(10);
            return resultSet.next();
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
            return true;
        }
    }

    public String query(String query) {
        try (Connection connection = DriverManager.getConnection(JDBC + database); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setQueryTimeout(10);
            ResultSet rs  = statement.executeQuery();
            return !rs.next() ? "" : rs.getString(1);
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void deleteUser(String username) {
        try (Connection con = DriverManager.getConnection(JDBC + database); Statement statement = con.createStatement()) {
            statement.setQueryTimeout(10);
            final String query = "Delete from users where username='" + username + "'";
            statement.execute(query);
            statement.closeOnCompletion();
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, null, ex);
        }
    }

    public static SqliteUserDatabase getInstance() {
        if (instance == null) {
            instance = new SqliteUserDatabase("users.db");
        }
        return instance;
    }
}
