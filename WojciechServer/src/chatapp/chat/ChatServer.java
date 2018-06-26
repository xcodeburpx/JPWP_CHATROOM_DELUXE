package chatapp.chat;

import chatapp.database.UserDatabase;


import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class ChatServer {
    static ChatRoomDatabase chatRoomDatabase = new ChatRoomDatabase();/* TODO: add chatRooms handling in clientHandler */
    static ArrayList<ClientHandler> connections = new ArrayList<>();
    static UserDatabase userDatabase = new UserDatabase();
    static private final int PORT = 25565;
    static private final int BACKLOG = 50;
    static private final String ADDR = "localhost";
    static private final Logger LOGGER = Logger.getLogger(ChatServer.class.getName());
    static private final String LOGFILE = "./src/chatapp/logs/";

    private ExecutorService executor = Executors.newFixedThreadPool(5);

    public static Logger getLOGGER() {
        return LOGGER;
    }

    public ChatServer() {
        try{
            File filepath = new File(LOGFILE);
            if (!filepath.exists()) {
                filepath.mkdirs();
            }
            FileHandler fh = new FileHandler(LOGFILE + "ChatServer.logs");
            LOGGER.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());
            try {
                InetAddress addr = InetAddress.getByName(ADDR);
                ServerSocket serverSocket = new ServerSocket(PORT, BACKLOG, addr);                             // creating listening socket

                while (true) {
                    LOGGER.log(Level.FINER, "Waiting for connection attempt..");
                    Socket clientSocket = serverSocket.accept();                                // accepting connection and binding it to its own socket
                    LOGGER.log(Level.FINER, "Connection established: " + clientSocket);

                    ClientHandler clientHandler = new ClientHandler(clientSocket);    // transferring client (socket) to its own thread
                    connections.add(clientHandler);
                    clientHandler.start();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.toString(), e);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ChatServer();
    }
}
