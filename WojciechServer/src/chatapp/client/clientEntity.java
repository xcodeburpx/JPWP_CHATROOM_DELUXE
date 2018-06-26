package chatapp.client;

import java.net.Socket;
import java.util.Map;

public class clientEntity {
    //TODO: Change definition of clientEntity
    private Integer ID;
    private String username;
    private Integer password;

    public clientEntity(UserLogInClass user, Socket socket) {
        for (Map.Entry<String, Integer> ur : user.getUserData().entrySet()) {
            username = ur.getKey();
            password = ur.getValue();
        }
    }

    public int getID() {
        return ID;
    }

    public String getUsername() {
        return username;
    }

    public boolean equals(clientEntity other){
        if(this.ID.equals(other.getID()) || this.username.equals(other.getUsername()))
            return true;
        return false;
    }

}
