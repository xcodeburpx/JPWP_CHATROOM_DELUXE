package chatapp.chat;

import chatapp.database.UserLogInClass;
import chatapp.net.NetMessage;
import chatapp.net.NetMessageType;
import chatapp.net.chatMessage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ClientHandler extends Thread implements clientObserver {


    private Socket clientSocket;
    private String userName;
    private boolean logged = false;
    private Logger LOGGER;
    private static FileHandler fh;
    private static String LOGFILE = "./src/chatapp/logs/";

    public ClientHandler(Socket clientSocket)           // receiving client socket transfer and chatRooms reference
    {
        try {
            this.clientSocket = clientSocket;
            // TODO: CHECK TIMEOUT HANDLING
            int timeout = 300;
            this.clientSocket.setSoTimeout(timeout*1000);
            StringBuilder name = new StringBuilder();
            name.append("ClientHandler-");
            name.append(this.clientSocket.toString().hashCode());
            name.append(".logs");
            LOGGER = Logger.getLogger(name.toString());

            File filepath = new File(LOGFILE);
            if (!filepath.exists()) {
                filepath.mkdirs();
            }

            fh = new FileHandler(LOGFILE + name);
            LOGGER.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());
            LOGGER.log(Level.FINEST, "Connected client: " + this.clientSocket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void sendMessageObj(NetMessage msg)   // sends message object to connected client
    {
        try {
            LOGGER.log(Level.FINE, "Sending begin...");
            ObjectOutputStream send = new ObjectOutputStream(clientSocket.getOutputStream());
            LOGGER.log(Level.FINE, "Done.");

            LOGGER.log(Level.FINE, "Sending " + msg.getType() + "...");
            send.writeObject(msg);
            LOGGER.log(Level.FINE, "Done.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    private NetMessage receiveMessageObj()  throws SocketTimeoutException  // returns received message from connected client
    {
        try {
            LOGGER.log(Level.FINE, "Receiving...");
            ObjectInputStream receive = new ObjectInputStream(clientSocket.getInputStream());       // creating two-way communication using objects
            LOGGER.log(Level.FINE, "Done.");

            LOGGER.log(Level.FINE, "Message received.");
            return (NetMessage) receive.readObject();
        }
        catch (SocketTimeoutException e){
            throw new SocketTimeoutException("Socket Timeout");
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            return null;
        }

    }

    public String getUserName() {
        return userName;
    }

    public boolean isLogged() {
        return logged;
    }

    public void update(chatMessage message) {
        NetMessage msg = new NetMessage(NetMessageType.MSG, message);
        sendMessageObj(msg);
    }

    public void info(NetMessage message) {
        sendMessageObj(message);
    }

    // TODO: IMPLEMENTATION AND ERROR CHECKING
    public void catchUp(Vector<chatMessage> lastMessages){                                                                 // get last 5 messages if available
        int size  = lastMessages.size() > 5 ? 5 : lastMessages.size();

        for(int i=0; i < size; ++i) {
            update(lastMessages.get(i));
        }
    }

    private void handleClient() throws IOException, NullPointerException                   // handles all the server - client logic and communication
    {

        boolean keepAlive = true;
        NetMessage msg;

        while (keepAlive) {

            try {

                msg = receiveMessageObj();
                boolean answer;
                int deleteAnswer;
                UserLogInClass user;
                String kappa;
                chatRoom room;
                chatMessage chatMsg;


                if (msg == null) {
                    System.out.println("Client close");
                    clientSocket.close();
                    return;
                }

                switch (msg.getType()) {
                    case CONNECTION_REQUEST:

                        msg = new NetMessage(NetMessageType.CONNECTION_RESPONSE, "CONNECTION_ACCEPTED");
                        sendMessageObj(msg);
                        break;

                    case CREATE_ACCOUNT_REQUEST:
                        user = (UserLogInClass) msg.getContent();
                        userName = user.getUsername();
                        answer = ChatServer.userDatabase.newUser(user.getUserData());
                        if(answer) {
                            msg = new NetMessage(NetMessageType.CREATE_ACCOUNT_RESPONSE, "ACCOUNT_CREATED");
                            sendMessageObj(msg);
                        } else {
                            msg = new NetMessage(NetMessageType.ERROR_SERVER, "ACCOUNT_CREATE_FAILED");
                            sendMessageObj(msg);
                        }
                        break;

                    case LOGIN_REQUEST:

                        user = (UserLogInClass) msg.getContent();
                        userName = user.getUsername();
                        answer = ChatServer.userDatabase.isUserLogged(user.getUserData(), ChatServer.connections);

                        if (answer) {
                            msg = new NetMessage(NetMessageType.ERROR_SERVER, "USER_DUPLICATED");
                            sendMessageObj(msg);
                        } else {
                            if (ChatServer.userDatabase.doesUserExists(user.getUserData())) {

                                logged = true;

                                msg = new NetMessage(NetMessageType.LOGIN_RESPONSE, "LOGIN_SUCCESSFUL");
                                sendMessageObj(msg);
                            } else {

                                msg = new NetMessage(NetMessageType.ERROR_SERVER, "USER_DOES_NOT_EXIST");
                                sendMessageObj(msg);
                            }

                        }
                        String temp = (String) msg.getContent();
                        System.out.println(temp);
                        break;

                    case CHATROOM_EXISTS_REQUEST:
                        String chatName = (String) msg.getContent();
                        Integer chatRoomID = chatName.hashCode();
                        room = ChatServer.chatRoomDatabase.getChatRoom(chatRoomID);
                        if (room == null) {
                            msg = new NetMessage(NetMessageType.CHATROOM_DOES_NOT_EXIST_RESPONSE, "CHATROOM_DOES_NOT_EXIST");
                            sendMessageObj(msg);
                        } else {
                            msg = new NetMessage(NetMessageType.CHATROOM_EXISTS_RESPONSE, "CHATROOM_DOES_EXIST");
                            sendMessageObj(msg);
                        }
                        break;


                    case CHATROOM_CREATE_REQUEST:

                        kappa = (String) msg.getContent();
                        answer = ChatServer.chatRoomDatabase.addChatRoom(kappa, userName);
                        if (!answer) {
                            msg = new NetMessage(NetMessageType.ERROR_SERVER, "CREATION_DENIED");
                            sendMessageObj(msg);
                        } else {
                            msg = new NetMessage(NetMessageType.CHATROOM_CREATE_RESPONSE, "ACTION_CREATED");
                            sendMessageObj(msg);
                            ChatServer.chatRoomDatabase.getChatRoom(kappa.hashCode()).subscribe(this);
                        }
                        break;

                    case CHATROOM_DELETE_REQUEST:

                        /* TODO: add check if no permissions for such action */

                        kappa = (String) msg.getContent();
                        deleteAnswer = ChatServer.chatRoomDatabase.deleteChatRoom(kappa.hashCode(), userName);
                        if (deleteAnswer!=0) {
                            if (deleteAnswer==-2) {
                                msg = new NetMessage(NetMessageType.ERROR_SERVER, "CHATROOM_DELETE_NO_SUCH_CHATROOM");
                                sendMessageObj(msg);
                                break;
                            } else if (deleteAnswer==-1) {
                                msg = new NetMessage(NetMessageType.ERROR_SERVER, "CHATROOM_DELETE_NOT_AUTHORIZED");
                                sendMessageObj(msg);
                                break;
                            }
                        } else {
                            msg = new NetMessage(NetMessageType.CHATROOM_DELETE_RESPONSE, "CHATROOM_DELETE_SUCCESS");
                            sendMessageObj(msg);
                        }

                        break;

                    case CHATROOM_JOIN_REQUEST:
                        kappa = (String) msg.getContent();
                        room = ChatServer.chatRoomDatabase.getChatRoom(kappa.hashCode());
                        if (room == null) {
                            msg = new NetMessage(NetMessageType.ERROR_SERVER, "CHATROOM_JOIN_ERROR_NOT_EXISTED");
                            sendMessageObj(msg);
                        } else {
                            if (!room.subscribe(this)) {
                                msg = new NetMessage(NetMessageType.ERROR_SERVER, "CHATROOM_JOIN_ERROR_CANNOT_LINK");
                                sendMessageObj(msg);
                            } else {
                                msg = new NetMessage(NetMessageType.CHATROOM_JOIN_RESPONSE, "CHATROOM_JOIN_SUCCESS");
                                sendMessageObj(msg);
//                                catchUp(ChatServer.chatRoomDatabase.getChatRoom(kappa.hashCode()).getMessages());
                            }
                        }

                        break;

                    case CHATROOM_LEAVE_REQUEST:
                        kappa = (String) msg.getContent();
                        room = ChatServer.chatRoomDatabase.getChatRoom(kappa.hashCode());
                        if (room == null) {
                            msg = new NetMessage(NetMessageType.ERROR_SERVER, "CHATROOM_LEAVE_ERROR_NOT_EXISTED");
                            sendMessageObj(msg);
                        } else {
                            if (!room.unsubscribe(this)) {
                                msg = new NetMessage(NetMessageType.ERROR_SERVER, "CHATROOM_LEAVE_ERROR_CANNOT_UNLINK");
                                sendMessageObj(msg);
                            } else {
                                msg = new NetMessage(NetMessageType.CHATROOM_LEAVE_RESPONSE, "CHATROOM_LEAVE_SUCCESS");
                                sendMessageObj(msg);
                            }
                        }

                        break;

                    case LOGOUT_REQUEST:

                        if (ChatServer.connections.contains(this)) {
                            for (chatRoom urRoom : ChatServer.chatRoomDatabase.getActiveChatRooms())      // unsubscribe from all chatrooms
                                if (urRoom.isUserPresent(this))
                                    urRoom.unsubscribe(this);

                            msg = new NetMessage(NetMessageType.LOGOUT_RESPONSE, "LOGOUT_SUCCESS");
                            sendMessageObj(msg);

                            ChatServer.connections.remove(this);

                        } else {
                            msg = new NetMessage(NetMessageType.ERROR_SERVER, "INVALID_REQUEST_NOT_LOGGED_IN");
                            sendMessageObj(msg);
                        }

                        keepAlive = false;
                        break;

                    case MSG:

                        chatMsg = (chatMessage) msg.getContent();
                        chatRoomID = chatMsg.getChatRoom().hashCode();
                        room = ChatServer.chatRoomDatabase.getChatRoom(chatRoomID);

                        if (room == null) {
                            msg = new NetMessage(NetMessageType.ERROR_SERVER, "ROOM_DOES_NOT_EXIST");
                            sendMessageObj(msg);
                        } else {
                            room.update(chatMsg);           // not sending response - all users are informed by room (update)
                        }

                        break;

                    case ERROR_CLIENT:

                        /* TODO: handle errors from client */

                        break;

                    default:
                        msg = new NetMessage(NetMessageType.ERROR_SERVER, "UNRESOLVED_REQUEST");
                        sendMessageObj(msg);
                        break;
                }
            } catch (NullPointerException e) {
                LOGGER.log(Level.SEVERE, e.toString(), e);
            } catch (SocketTimeoutException e) {
                ChatServer.connections.remove(this);
                keepAlive= false;
                clientSocket.close();
            }

        }

        clientSocket.close();
    }

    @Override
    public void run() {
        try {
            handleClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
