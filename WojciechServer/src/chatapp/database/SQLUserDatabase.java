package chatapp.database;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SQLUserDatabase {
    private Connection c = null;
    private Statement statement = null;
    private static FileHandler fh;
    private static final Logger LOGGER = Logger.getLogger(SQLUserDatabase.class.getName());
    static private final String LOGFILE = "./src/chatapp/logs/";


    public SQLUserDatabase() {
        try {
                File filepath = new File(LOGFILE);
                if (!filepath.exists()) {
                    filepath.mkdirs();
                }
                fh = new FileHandler(LOGFILE + "SQLUserDatabase.logs");
                LOGGER.addHandler(fh);
                fh.setFormatter(new SimpleFormatter());
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:users.db");
                c.setAutoCommit(false);
                LOGGER.log(Level.FINE, "Opened database successfully");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.toString(), e);
                System.exit(0);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public HashMap<String, Integer> queryDatabase() {
        HashMap<String, Integer> users = new HashMap<>();

        try {
            statement = c.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM USERS;");

            while (rs.next()) {
                String username = rs.getString("username");
                int password = rs.getInt("password");
                users.put(username, password);
            }
            rs.close();
            statement.close();
            c.close();

            return users;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            return null;
        }

    }

    public boolean addUser(HashMap<String, Integer> user) {
        try {
            statement = c.createStatement();
            String username = null;
            Integer password = null;
            for (Map.Entry<String, Integer> temp : user.entrySet()) {
                username = temp.getKey();
                password = temp.getValue();
            }

            String sql = "SELECT count(*) FROM USERS where USERNAME = " + "'" + username + "'" + ";";
            ResultSet rst = statement.executeQuery(sql);

            if (rst.getInt(1) > 0) {
                LOGGER.log(Level.INFO, "USER DUPLICATION");
                return false;
            }

            sql = "INSERT INTO USERS (USERNAME, PASSWORD) " +
                    "VALUES (" + "'" + username + "'," + password.intValue() + " );";
            statement.executeUpdate(sql);
            c.commit();

            statement.close();
            c.close();

            LOGGER.log(Level.FINE, "USER CREATED");
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            return false;
        }
    }

    public boolean deleteUser(String user) {
        try {
            statement = c.createStatement();
            String sql = "DELETE FROM USERS WHERE USERNAME = " + user + ";";
            statement.executeUpdate(sql);
            c.commit();
            statement.close();
            c.close();
            LOGGER.log(Level.INFO, "USER DELETED");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            return false;
        }
    }

    public boolean checkUser(HashMap<String, Integer> user) {
        try {
            statement = c.createStatement();
            String username = null;
            Integer password = null;
            for (Map.Entry<String, Integer> temp : user.entrySet()) {
                username = temp.getKey();
                password = temp.getValue();
            }

            String sql = "SELECT count(*) FROM USERS where USERNAME = " + "'" + username + "'" + " AND PASSWORD = " + password.intValue() + ";";
            ResultSet rst = statement.executeQuery(sql);

            if (rst.getInt(1) <= 0) {
                LOGGER.log(Level.INFO, "NO SUCH USER");
                return false;
            }

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            return false;
        }
    }
}
