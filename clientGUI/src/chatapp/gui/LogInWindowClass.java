package chatapp.gui;

import java.util.HashMap;
import java.util.Map;

public class LogInWindowClass {
    private HashMap<String, Integer> userData;
    private String username;
    private int windowOption;

    public LogInWindowClass(HashMap<String, Integer> user, int option) {
        userData = user;
        windowOption = option;
        for(Map.Entry<String, Integer> us: user.entrySet()) {
            username = us.getKey();
        }
    }

    public HashMap<String, Integer> getUserData() {
        return userData;
    }

    public String getUsername() {
        return username;
    }

    public int getWindowOption() {
        return windowOption;
    }
}
