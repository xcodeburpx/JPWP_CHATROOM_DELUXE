package chatapp.database;

import chatapp.chat.ChatServer;
import chatapp.chat.ClientHandler;

import java.util.*;
import java.util.logging.Level;

public class UserDatabase {
    SQLUserDatabase sqlUserDatabase = new SQLUserDatabase();
    static ArrayList<String> logInUsers = new ArrayList<>();
    public UserDatabase() {}

    public synchronized boolean newUser(HashMap<String, Integer> user) {
        try {
            String username = null;
            Integer password = null;
            for (Map.Entry<String, Integer> temp: user.entrySet()) {
                username = temp.getKey();
                password = temp.getValue();
            }

            if (username.equals("") || password.equals(null)) {
                return false;
            }

            return sqlUserDatabase.addUser(user);

        } catch (Exception e) {
            ChatServer.getLOGGER().log(Level.SEVERE, e.toString(), e);
            return false;
        }
    }

    public synchronized boolean isUserLogged(HashMap<String, Integer> userData, ArrayList<ClientHandler> connections) {

        for(Map.Entry<String, Integer> ur: userData.entrySet()){       // Get UserName
            for(ClientHandler conn: connections)
                if(conn.getUserName().equals(ur.getKey()))
                    if(conn.isLogged())
                        return true;
        }

        return false;
    }

    public synchronized boolean doesUserExists(HashMap<String, Integer> userData){
        if(sqlUserDatabase.checkUser(userData))
            return true;
        return false;
    }

}
