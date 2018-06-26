package chatapp.net;

import chatapp.database.UserLogInClass;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

import static java.lang.Thread.sleep;

public class Main{

    private static void sendMessageObj(NetMessage msg, Socket clientSocket)   // sends message object to connected client
    {
        try
        {
            System.out.print("Opening comms...");
            ObjectOutputStream send = new ObjectOutputStream(clientSocket.getOutputStream());
            System.out.print("Done.\n");

            System.out.print("Sending...");
            send.writeObject(msg);
            System.out.print("Done.\n");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private static NetMessage receiveMessageObj(Socket clientSocket)         // returns received message from connected client
    {
        try
        {
            System.out.print("Opening comms...");
            ObjectInputStream receive = new ObjectInputStream(clientSocket.getInputStream());       // creating two-way communication using objects
            System.out.print("Done.\n");

            return (NetMessage) receive.readObject();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    public static void main(String[] args) {
        boolean isConnected = false;
        NetMessage msg = null;

        while(!isConnected) {
            try {
                Socket clientSocket = new Socket("127.0.0.1", 25565);
                System.out.println("Connected");
                isConnected = true;

                // User login
                HashMap<String, String> ur = new HashMap<>();
                ur.put("Marco", "Polo");
                UserLogInClass user = new UserLogInClass(ur, 0);
                msg = new NetMessage(NetMessageType.LOGIN_REQUEST, user);

                sendMessageObj(msg, clientSocket);
                sleep(500);
                msg = receiveMessageObj(clientSocket);
                System.out.println("Message type: " + msg.getType() + " Answer: "
                        + msg.getContent());


                // Create chatroom
                String chatRoom = "Test1";
                msg = new NetMessage(NetMessageType.CHATROOM_CREATE_REQUEST, chatRoom);

                sendMessageObj(msg, clientSocket);
                sleep(500);
                msg = receiveMessageObj(clientSocket);
                System.out.println("Message type: " + msg.getType() + " Answer: "
                                    + msg.getContent());

                // Join chatroom
                chatRoom = "Test1";
                msg = new NetMessage(NetMessageType.CHATROOM_JOIN_REQUEST, chatRoom);

                sendMessageObj(msg, clientSocket);
                sleep(500);
                msg = receiveMessageObj(clientSocket);
                System.out.println("Message type: " + msg.getType() + " Answer: "
                        + msg.getContent());

                // Send message
                chatMessage chatMsg = new chatMessage("Test1", "Marco", "MyNameIsMarcos");
                msg = new NetMessage(NetMessageType.MSG, chatMsg);

                sendMessageObj(msg, clientSocket);
                sleep(500);
                msg = receiveMessageObj(clientSocket);
                System.out.println("Message type: " + msg.getType() + " Answer: "
                        + msg.getContent());


                // Leave chatroom
//                chatRoom = "Test1";
//                msg = new NetMessage(NetMessageType.CHATROOM_LEAVE_REQUEST, chatRoom);
//
//                sendMessageObj(msg, clientSocket);
//                sleep(500);
//                msg = receiveMessageObj(clientSocket);
//                System.out.println("Message type: " + msg.getType() + " Answer: "
//                        + msg.getContent());

                // Delete chatroom
                chatRoom = "Test1";
                msg = new NetMessage(NetMessageType.CHATROOM_DELETE_REQUEST, chatRoom);

                sendMessageObj(msg, clientSocket);
                sleep(500);
                msg = receiveMessageObj(clientSocket);
                System.out.println("Message type: " + msg.getType() + " Answer: "
                        + msg.getContent());

                // Logout
                msg = new NetMessage(NetMessageType.LOGOUT_REQUEST,null);

                sendMessageObj(msg, clientSocket);
                msg = receiveMessageObj(clientSocket);
                System.out.println("Message type: " + msg.getType() + " Answer: "
                        + msg.getContent());

                clientSocket.close();
                System.out.println("Socket closed. Exiting...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
