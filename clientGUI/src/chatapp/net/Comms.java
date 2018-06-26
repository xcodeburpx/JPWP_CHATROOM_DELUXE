package chatapp.net;

import chatapp.gui.ClientGUI;
import chatapp.logic.ChatLogic;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Comms {
    private static final Logger LOGGER = ChatLogic.getLOGGER();

    public static void sendMessageObj(NetMessage msg, Socket clientSocket) // sends message object to connected client
    {
        try
        {
            LOGGER.log(Level.FINE,"Sending begin...");
            ObjectOutputStream send = new ObjectOutputStream(clientSocket.getOutputStream());
            LOGGER.log(Level.FINE,"Done.");

            LOGGER.log(Level.FINE,"Sending...");
            send.writeObject(msg);
            LOGGER.log(Level.FINE,"Done.");
        }
        catch(Exception e)
        {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }


    public static NetMessage receiveMessageObj(Socket clientSocket)  throws EOFException       // returns received message from connected client
    {
        try
        {
            LOGGER.log(Level.FINE,"Opening comms...");
            ObjectInputStream receive = new ObjectInputStream(clientSocket.getInputStream());       // creating two-way communication using objects
            LOGGER.log(Level.FINE,"Done.");

            return (NetMessage) receive.readObject();
        }
        catch(EOFException e){
            throw new EOFException("EOFException");
        }
        catch(Exception e)
        {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            return null;
        }
    }
}
