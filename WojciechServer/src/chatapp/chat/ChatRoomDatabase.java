package chatapp.chat;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ChatRoomDatabase {
    static HashMap<Integer, chatRoom> activeChatRooms = new HashMap<>(); // Map of existing rooms
    private static FileHandler fh;
    static private String LOGFILE = "./src/chatapp/logs/";
    private static final Logger LOGGER = Logger.getLogger(ChatRoomDatabase.class.getName());

    public ChatRoomDatabase(){
        try{
            File filepath = new File(LOGFILE);
            if (!filepath.exists()) {
                filepath.mkdirs();
            }
            fh = new FileHandler(LOGFILE + "ChatRoomDatabase.logs");
            LOGGER.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean addChatRoom(String chatRoom, String adminName){         // add room to server
        Integer chatRoomID= chatRoom.hashCode();
        if(chatRoomExists(chatRoomID))
            return false;
        activeChatRooms.put(chatRoomID, new chatRoom(chatRoom, chatRoomID, adminName));
        LOGGER.log(Level.FINE,"Chatroom created, ID: " + chatRoomID);
        return true;
    }

    public int deleteChatRoom(Integer chatRoomID, String adminName) {     // delete room form server
        if(chatRoomExists(chatRoomID)) {
            if(activeChatRooms.get(chatRoomID).getAdminName().equals(adminName)) {
                activeChatRooms.get(chatRoomID).autoLogOut();
                activeChatRooms.remove(chatRoomID);
                LOGGER.log(Level.FINE, "Chatroom deleted, ID: " + chatRoomID);
                return 0;
            } else {
                return -1;
            }
        }
        return -2;
    }

    private boolean chatRoomExists(Integer roomID){         // check for room existence
        if(activeChatRooms.containsKey(roomID))
            return true;
        return false;
    }

    public synchronized chatRoom getChatRoom(Integer roomID){
        if(chatRoomExists(roomID)){
            return activeChatRooms.get(roomID);
        }
        return null;
    }

    public  ArrayList<chatRoom> getActiveChatRooms() {
        ArrayList<chatRoom> rooms = new ArrayList<>();
        for(Map.Entry<Integer, chatRoom> room: activeChatRooms.entrySet())
            rooms.add(room.getValue());

        return rooms;
    }

    public void kill(int id){
        activeChatRooms.remove(id);
        LOGGER.log(Level.INFO, "Chatroom killed, ID: " + id);
    }
}
