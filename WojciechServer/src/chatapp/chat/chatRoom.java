package chatapp.chat;

import chatapp.net.NetMessage;
import chatapp.net.NetMessageType;
import chatapp.net.chatMessage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class chatRoom
{
    private int myID;
    private String myName;
    private String adminName;
    private ArrayList<ClientHandler> usersInRoom;
    private Vector<String> userNames = new Vector<>();
    private Logger LOGGER;
    private FileHandler fh;
    private static final String LOGFILE = "./src/chatapp/logs/";
    private Vector<chatMessage> messages = new Vector<>();
    private ArrayList<Object> infoArray = new ArrayList<>();
    private ArrayList<Object> deleteArray = new ArrayList<>();
    private PrintWriter chatLog;

    public chatRoom(String name,int id, String adminName) {
        myName = name;
        myID = id;
        usersInRoom = new ArrayList<>();

        infoArray.add(myName);
        infoArray.add(null);

        deleteArray.add(myName);
        deleteArray.add(null);

        this.adminName = adminName;

        // TODO: CREATE A FILE FOR CHATLOGS

        try {
            File filepath = new File("./chatlogs/");
            if (!filepath.exists()) {
                filepath.mkdirs();
            }
            File chatfile = new File(filepath+"/Log" + name + Integer.toString(id) + new java.util.Date() +".logs");
            chatLog = new PrintWriter(new FileOutputStream(chatfile));
        } catch(IOException e) {
            e.printStackTrace();
        }


        LOGGER = Logger.getLogger(chatRoom.class.getName() + "-" + myID);
        try{
            File filepath = new File(LOGFILE);
            if (!filepath.exists()) {
                filepath.mkdirs();
            }
            fh = new FileHandler(LOGFILE + "chatRoom" + "-" + myID + ".logs");
            LOGGER.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public Vector<chatMessage> getMessages() {
        return messages;
    }

    public String getAdminName() {return this.adminName;}

    public synchronized boolean subscribe(ClientHandler user){          // links user to this chatroom
        for(ClientHandler usInChat: usersInRoom) {
            if(usInChat == user){  // check if user is already subscribed
                return false;
            }
        }
        //TODO: SEND INFORMATION ABOUT NEW USER
        usersInRoom.add(user);
        userNames.addElement(user.getUserName());
        infoArray.set(1, userNames);
        updateInfo(infoArray);

        chatLog.print("* " + user.getUserName() + " joined the room *\n");

        LOGGER.log(Level.FINE,"User joined, username: " + user.getUserName());
        return true;
    }

    public synchronized boolean unsubscribe(ClientHandler user) {// removes user from chatroom
        for(ClientHandler usInChat: usersInRoom){
            if(usInChat == user){  // check if user is in chatroom
                usersInRoom.remove(user);
                userNames.remove(user.getUserName());
                LOGGER.log(Level.FINE, "User removed, username: " + user.getUserName());
                deleteArray.set(1, user.getUserName());
                deleteInfo(deleteArray);

                chatLog.print("* " + user.getUserName() + " left the room *\n");

                if(usersInRoom.isEmpty())
                    autoLogOut();
                return true;
            }
        }

        return false;
    }

    public boolean isUserPresent(ClientHandler user){
        for(ClientHandler ur: usersInRoom)
            if(ur == user)
                return true;

        return false;
    }

    public synchronized void update(chatMessage message){
        messages.addElement(message);

        chatLog.println(message.getClientName() + ": " + message.getMessageContent());

        for(ClientHandler usUpdate: usersInRoom){
            usUpdate.update(message);
        }
    }

    public synchronized void deleteInfo(ArrayList<Object> delete){
        NetMessage msg = new NetMessage(NetMessageType.USER_DELETED, delete);
        for(ClientHandler usUpdate: usersInRoom){
            usUpdate.info(msg);
        }
    }

    public synchronized void updateInfo(ArrayList<Object> info){
        NetMessage msg = new NetMessage(NetMessageType.USER_ADDED,info);
        for(ClientHandler usUpdate: usersInRoom){
            usUpdate.info(msg);
        }
    }

    public void autoLogOut(){
        ChatServer.chatRoomDatabase.kill(myID);
        chatLog.print("* Chatroom has been killed *");
        chatLog.close();
        LOGGER.log(Level.FINE, "ChatRoom deleted");
    }
}
