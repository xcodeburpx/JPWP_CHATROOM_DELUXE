package chatapp.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserLogInClass implements Serializable{
    private HashMap<String, Integer> userData = new HashMap<>();
    private String username;
    private int windowOption;

    public UserLogInClass(HashMap<String, String> user, int option) {
        for(Map.Entry<String,String> ur: user.entrySet()){
            userData.put(ur.getKey(), toHashCode(user));
            username =  ur.getKey();
        }
        windowOption = option;
    }

    private Integer toHashCode(HashMap<String, String> user){
        for(Map.Entry<String,String> ur: user.entrySet()){
            return ur.getValue().hashCode();
        }
        return null;
    }

    public String getUsername() {
        return username;
    }

    public int getWindowOption() {
        return windowOption;
    }

    public HashMap<String, Integer> getUserData() {
        return userData;
    }
}
