package chatapp.logic;

import chatapp.database.UserLogInClass;
import chatapp.gui.ClientGUI;
import chatapp.net.Comms;
import chatapp.net.NetMessage;
import chatapp.net.NetMessageType;
import chatapp.net.chatMessage;

import javax.swing.*;
import java.awt.*;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class ChatLogic extends Thread {

    static boolean isConnected = false;
    private int timeout = 1000;
    private ClientGUI gui;
    private Socket clientSocket;
    private NetMessage signalMessage = null;
    private static FileHandler fh;
    private static final Logger LOGGER = Logger.getLogger(ChatLogic.class.getName());
    private static String LOGFILE = "./src/chatapp/logs/ChatLogic";

    public ChatLogic(ClientGUI gui) {
        this.gui = gui;
        Random rand = new Random();

        try {
            File filepath = new File(LOGFILE);
            if (!filepath.exists()) {
                filepath.mkdirs();
            }
            fh = new FileHandler(LOGFILE + rand.nextInt() + ".logs");
            LOGGER.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getLOGGER() {
        return LOGGER;
    }

    public boolean connectSimulation(String ipAddress) {
        //TODO: SoTimeout to check connectivity
        return this.connect(ipAddress);
    }

    private void receiveLoop() {
        while (signalMessage == null) {
            try {
                sleep(50);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, e.toString(), e);
            }
        }
    }

    public boolean connect(String ipAddress) {
        try {
            clientSocket = new Socket(ipAddress, 25565);
            clientSocket.setSoTimeout(timeout);
            NetMessage msg = new NetMessage(NetMessageType.CONNECTION_REQUEST, null);
            Comms.sendMessageObj(msg, clientSocket);
            msg = Comms.receiveMessageObj(clientSocket);
            if (msg == null) {
                signalMessage = null;
                return false;
            } else if (msg.getType() == NetMessageType.CONNECTION_RESPONSE) {
                signalMessage = null;
                clientSocket.setSoTimeout(0);
                return true;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            signalMessage = null;
            return false;
        }
        signalMessage = null;
        return false;
    }

    public int determine(UserLogInClass userData) {
        if (userData.getWindowOption() == 0)
            return manageLogIn(userData);
        return manageCreate(userData);
    }

    private int manageLogIn(UserLogInClass userData) {
        NetMessage msg = new NetMessage(NetMessageType.LOGIN_REQUEST, userData);
        Comms.sendMessageObj(msg, clientSocket);
        try {
            msg = Comms.receiveMessageObj(clientSocket);
            switch (msg.getType()) {
                //TODO: ADD DISTINCTION BETWEEN ERROR-SERVER MESSAGES
                case LOGIN_RESPONSE:
                    signalMessage = null;
                    isConnected = true;
                    return 0;
                case ERROR_SERVER:
                    signalMessage = null;
                    String message = (String) msg.getContent();
                    if(message.equals("USER_DUPLICATED"))
                        return -1;
                    else if(message.equals("USER_DOES_NOT_EXIST"))
                        return -2;
            }
            signalMessage = null;
            return -3;
        } catch (EOFException e) {
            return -3;
        }
    }

    private int manageCreate(UserLogInClass userData) {
        NetMessage msg = new NetMessage(NetMessageType.CREATE_ACCOUNT_REQUEST, userData);
        Comms.sendMessageObj(msg, clientSocket);
        try {
            msg = Comms.receiveMessageObj(clientSocket);
            switch (msg.getType()) {
                //TODO: ADD DISTINCTION BETWEEN ERROR-SERVER MESSAGES
                case CREATE_ACCOUNT_RESPONSE:
                    signalMessage = null;
                    isConnected = true;
                    return 0;
                case ERROR_SERVER:
                    signalMessage = null;
                    return -1;
            }
            signalMessage = null;
            return -1;
        } catch (EOFException e) {
            return -1;
        }
    }

    public void createChatRoom() {
        gui.getMainWindow().setEnabled(false);
        boolean loopVar = true;
        String labelName = "Enter the name of new chatRoom";
        String windowName = "Create Chat Room";
        while (loopVar) {
            ArrayList<Object> objList = windowADJL(labelName, windowName);
            int option = (Integer) objList.get(0);
            String chatRoomName = (String) objList.get(1);

            if (option == 2) {
                gui.getMainWindow().setEnabled(true);
                loopVar = false;
            } else {
                gui.getPeopleInRoom().put(chatRoomName, new Vector<>());
                NetMessage msg = new NetMessage(NetMessageType.CHATROOM_CREATE_REQUEST, chatRoomName);
                Comms.sendMessageObj(msg, clientSocket);
                receiveLoop();
                switch (signalMessage.getType()) {
                    case CHATROOM_CREATE_RESPONSE:
                        signalMessage = null;
                        gui.addTab(chatRoomName);
                        loopVar = false;
                        gui.getMainWindow().setEnabled(true);
                        break;
                    case ERROR_SERVER:
                        signalMessage = null;
                        labelName = "Room exists.\n Enter another name";
                        gui.getPeopleInRoom().remove(chatRoomName);
                        loopVar = true;
                        break;
                    default:
                        loopVar = true;
                        labelName = "Something went wrong.\n Enter another name";
                        gui.getPeopleInRoom().remove(chatRoomName);
                        signalMessage = null;
                }
            }
        }

    }

    public void deleteChatRoom() {
        gui.getMainWindow().setEnabled(false);
        boolean loopVar = true;
        String labelName = "Enter the chatRoom to delete";
        String windowName = "Delete Chat Room";


        while (loopVar) {
            ArrayList<Object> objList = windowADJL(labelName, windowName);
            int option = (Integer) objList.get(0);
            String chatRoomName = (String) objList.get(1);

            if (option == 2) {
                gui.getMainWindow().setEnabled(true);
                loopVar = false;
            } else {
                if (chatRoomName.equals("Welcome!")) {
                    this.infoWindow("You cannot delete 'Welcome!' window!!", "'Welcome!' delete error!");
                    loopVar = true;
                } else {
                    NetMessage msg = new NetMessage(NetMessageType.CHATROOM_DELETE_REQUEST, chatRoomName);
                    Comms.sendMessageObj(msg, clientSocket);
                    receiveLoop();
                    switch (signalMessage.getType()) {
                        case CHATROOM_DELETE_RESPONSE:
                            signalMessage = null;
                            gui.deleteTab(chatRoomName);
                            loopVar = false;
                            gui.getMainWindow().setEnabled(true);
                            break;
                        case ERROR_SERVER:
                            String message = (String) signalMessage.getContent();
                            System.out.println(message);
                            if(message.equals("CHATROOM_DELETE_NO_SUCH_CHATROOM"))
                                labelName = "Room does not exist.\n Enter another name";
                            else if(message.equals("CHATROOM_DELETE_NOT_AUTHORIZED"))
                                labelName = "You are not authorized\n Enter another name";
                            loopVar = true;
                            signalMessage = null;
                            break;
                        default:
                            loopVar = true;
                            labelName = "Something went wrong.\n Enter another name";
                            signalMessage = null;
                    }
                }

            }
        }
    }

    public void joinChatRoom() {
        gui.getMainWindow().setEnabled(false);
        boolean loopVar = true;
        String labelName = "Enter the chatRoom to join";
        String windowName = "Join Chat Room";

        while (loopVar) {

            ArrayList<Object> objList = windowADJL(labelName, windowName);
            int option = (Integer) objList.get(0);
            String chatRoomName = (String) objList.get(1);

            if (option == 2) {
                gui.getMainWindow().setEnabled(true);
                loopVar = false;
            } else {
                gui.getPeopleInRoom().put(chatRoomName, new Vector<>());
                NetMessage msg = new NetMessage(NetMessageType.CHATROOM_JOIN_REQUEST, chatRoomName);
                Comms.sendMessageObj(msg, clientSocket);
                receiveLoop();
                switch (signalMessage.getType()) {
                    case CHATROOM_JOIN_RESPONSE:
                        signalMessage = null;
                        gui.addTab(chatRoomName);
                        loopVar = false;
                        gui.getMainWindow().setEnabled(true);
                        break;
                    case ERROR_SERVER:
                        signalMessage = null;
                        labelName = "Room does not exist.\n Enter another name";
                        gui.getPeopleInRoom().remove(chatRoomName);
                        loopVar = true;
                        break;
                    default:
                        loopVar = true;
                        labelName = "Something went wrong.\n Enter another name";
                        gui.getPeopleInRoom().remove(chatRoomName);
                        signalMessage = null;
                }
            }
        }
    }

    public void leaveChatRoom() {
        gui.getMainWindow().setEnabled(false);
//        boolean loopVar = true;
//        String labelName = "Are you sure?";
//        String windowName = "Leaving Chatroom";


//        ArrayList<Object> objList = windowADJL(labelName, windowName);
//        int option = (Integer) objList.get(0);
//        String chatRoomName = (String) objList.get(1);
        String chatRoomName = this.gui.getActualWindow();
        if (chatRoomName.equals("Welcome!")){
            gui.getMainWindow().setEnabled(true);
            return;
        }


        NetMessage msg = new NetMessage(NetMessageType.CHATROOM_LEAVE_REQUEST, chatRoomName);
        Comms.sendMessageObj(msg, clientSocket);
        receiveLoop();

        switch (signalMessage.getType()) {
            case CHATROOM_LEAVE_RESPONSE:
                signalMessage = null;
                gui.deleteTab(chatRoomName);
                gui.getMainWindow().setEnabled(true);
                break;
            case ERROR_SERVER:
                signalMessage = null;
                break;
            default:
                this.infoWindow("Something went wrong", "SOMETHING WENT WRONG");
                signalMessage = null;
        }

//        while (loopVar) {
//            ArrayList<Object> objList = windowADJL(labelName, windowName);
//            int option = (Integer) objList.get(0);
//            String chatRoomName = (String) objList.get(1);
//
//            if (option == 2) {
//                gui.getMainWindow().setEnabled(true);
//                loopVar = false;
//            } else {
//
//                NetMessage msg = new NetMessage(NetMessageType.CHATROOM_LEAVE_REQUEST, chatRoomName);
//                Comms.sendMessageObj(msg, clientSocket);
//                receiveLoop();
//
//                switch (signalMessage.getType()) {
//                    case CHATROOM_LEAVE_RESPONSE:
//                        signalMessage = null;
//                        gui.deleteTab(chatRoomName);
//                        loopVar = false;
//                        gui.getMainWindow().setEnabled(true);
//                        break;
//                    case ERROR_SERVER:
//                        signalMessage = null;
//                        labelName = "Room does not exist.\n Enter another name";
//                        loopVar = true;
//                        break;
//                    default:
//                        loopVar = true;
//                        labelName = "Something went wrong.\n Enter another name";
//                        signalMessage = null;
//                }
//            }
//        }
    }

    public boolean logout() {
        NetMessage msg = new NetMessage(NetMessageType.LOGOUT_REQUEST, null);
        Comms.sendMessageObj(msg, clientSocket);
        receiveLoop();
        switch (signalMessage.getType()) {
            case LOGOUT_RESPONSE:
                signalMessage = null;
                return true;
            case ERROR_SERVER:
                signalMessage = null;
                return false;
        }
        signalMessage = null;
        return false;
    }

    public boolean ifChatExists(String chatRoomName) {
        NetMessage msg = new NetMessage(NetMessageType.CHATROOM_EXISTS_REQUEST, chatRoomName);
        Comms.sendMessageObj(msg, clientSocket);
        receiveLoop();
        switch (signalMessage.getType()) {
            case CHATROOM_EXISTS_RESPONSE:
                signalMessage = null;
                return true;
            case CHATROOM_DOES_NOT_EXIST_RESPONSE:
                signalMessage = null;
                this.infoWindow("ChatRoom does not exists.\n Tab will be deleted", "ChatRoom does ot exist!");
                gui.deleteTab(gui.getActualWindow());
                break;
            case ERROR_SERVER:
                signalMessage = null;
                return false;
        }
        signalMessage = null;
        return false;
    }

    public void sendChatMessage(chatMessage chatMsg) {
        NetMessage msg = new NetMessage(NetMessageType.MSG, chatMsg);
        if (this.ifChatExists(chatMsg.getChatRoom())) {
            Comms.sendMessageObj(msg, clientSocket);
        }
    }

    public void infoWindow(String windowMessage, String dialogMessage) {
        JOptionPane.showMessageDialog(this.gui.getMainWindow(), windowMessage,
                dialogMessage, JOptionPane.WARNING_MESSAGE);
    }

    private ArrayList<Object> windowADJL(String labelName, String windowName) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel field = new JPanel(new GridLayout(0, 1, 2, 2));
        JLabel label = new JLabel(labelName);

        field.add(label);
        panel.add(field, BorderLayout.NORTH);

        JLabel fieldName = new JLabel("Room Name");
        JTextField nameField = new JTextField();

        panel.add(fieldName, BorderLayout.WEST);
        panel.add(nameField, BorderLayout.CENTER);

        int number = JOptionPane.showConfirmDialog(this.gui.getMainWindow(), panel, windowName, JOptionPane.OK_CANCEL_OPTION);

        String chatRoomName = nameField.getText();
        ArrayList<Object> objectList = new ArrayList<>();
        objectList.add(number);
        objectList.add(chatRoomName);

        return objectList;
    }

    public void exitWindow() {
        gui.getMainWindow().setEnabled(false);
        int counter = 0;
        boolean ifSuccess = false;
        while (!ifSuccess) {
            int reply = JOptionPane.showConfirmDialog(this.gui.getMainWindow(), "Are you sure?", "Exit Dialogue", JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                if (this.logout()) {
                    JOptionPane.showMessageDialog(this.gui.getMainWindow(), "Successfully logged out", "Logout Success", JOptionPane.INFORMATION_MESSAGE);
                    this.gui.getMainWindow().dispose();
                    System.exit(1);
                } else {
                    counter += 1;
                    if (counter < 3) {
                        JOptionPane.showMessageDialog(this.gui.getMainWindow(), "Failure to logout: " +
                                (3 - counter) + " attempts to go", "Logout Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this.gui.getMainWindow(), "Failed to logout - force exit", "Forced Exit", JOptionPane.INFORMATION_MESSAGE);
                        this.gui.getMainWindow().dispose();
                        System.exit(1);
                    }
                }
            } else {
                this.gui.getMainWindow().setVisible(true);
                this.gui.getMainWindow().setEnabled(true);
                ifSuccess = true;
            }
        }
    }


    public boolean logInWindow() {
        HashMap<String, String> userData = new HashMap<>();
        int windowOption = -1;
        int answer = -3;
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JPanel fields = new JPanel(new GridLayout(0, 1, 2, 2));
        fields.add(new JLabel("Username", SwingUtilities.RIGHT));
        fields.add(new JLabel("Password", SwingUtilities.RIGHT));
        fields.add(new JLabel("IP Address", SwingUtilities.RIGHT));
        // TODO: LET USER CHANGE/CHOOSE SERVER IP
        panel.add(fields, BorderLayout.WEST);

        JPanel ctrls = new JPanel(new GridLayout(0, 1, 2, 2));
        JTextField username = new JTextField();
        ctrls.add(username);
        JPasswordField password = new JPasswordField();
        ctrls.add(password);
        JTextField ipAddress = new JTextField();
        ctrls.add(ipAddress);
        panel.add(ctrls, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JRadioButton logInButton = new JRadioButton("Login");
        JRadioButton createButton = new JRadioButton("Create");

        ButtonGroup group = new ButtonGroup();
        group.add(logInButton);
        group.add(createButton);
        logInButton.setSelected(true);

        buttonPanel.add(logInButton);
        buttonPanel.add(createButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        int number = JOptionPane.showConfirmDialog(null, panel, "Login", JOptionPane.OK_CANCEL_OPTION);

        if (number == 2) {
            System.exit(0);
            return false;
        }

        if (logInButton.isSelected()) {
            windowOption = 0;
        }

        if (createButton.isSelected()) {
            windowOption = 1;
        }

        String usname = username.getText();
        String psword = new String(password.getPassword());
        String ipAddr = ipAddress.getText().length()>0 ? ipAddress.getText() : "localhost";

        userData.put(usname, psword);

        if (usname.length() <= 0 || psword.length() <= 0) {
            this.infoWindow("Not enough data", "Username/Password error");
            return false;
        }

        if (this.connectSimulation(ipAddr)) {
            answer = this.determine(new UserLogInClass(userData, windowOption));
            if (answer == 0) {
                gui.setLoginUser(usname);
                return true;
            } else {
                if (logInButton.isSelected()) {
                    if(answer == -1)
                        this.infoWindow("User is logged in", "Login Failure");
                    else if(answer == -2)
                        this.infoWindow("User does not exist", "Login Failure");
                    return false;
                } else if (createButton.isSelected()) {
                    this.infoWindow("Cannot create account", "Account Creation Failure");
                    return false;
                } else {
                    this.infoWindow("NOT IMPLEMENTED!", "NOT IMPLEMENTED!");
                    return false;
                }
            }
        } else {
            this.infoWindow("Cannot connect to the server", "Connection Failure");
            return false;
        }
    }


    public void handleClient() {
        boolean keepAlive = true;
        chatMessage chatMsg;
        String chatName;
        NetMessage msg;

        try {
            while (keepAlive) {
                msg = Comms.receiveMessageObj(clientSocket);
                LOGGER.log(Level.INFO, "Message type: " + msg.getType());
                switch (msg.getType()) {

                    case MSG:

                        chatMsg = (chatMessage) msg.getContent();
                        chatName = chatMsg.getChatRoom();
                        if (gui.getChatMap().containsKey(chatName)) {
                            String constructMessage = chatMsg.getClientName() + " -> " + chatMsg.getMessageContent() + "\n";
                            gui.getChatMap().get(chatName).append(constructMessage);
                        }

                        break;

                    case USER_ADDED:
                        ArrayList<Object> info = (ArrayList<Object>) msg.getContent();
                        chatName = (String) info.get(0);
                        Vector<String> userNames = (Vector<String>) info.get(1);
                        gui.addUser(chatName, userNames);
                        break;

                    case USER_DELETED:
                        info = (ArrayList<Object>) msg.getContent();
                        chatName = (String) info.get(0);
                        String user = (String) info.get(1);
                        gui.deleteUser(chatName, user);
                        break;

                    case CHATROOM_DOES_NOT_EXIST_RESPONSE:
                        signalMessage = msg;
                        break;

                    case CHATROOM_EXISTS_RESPONSE:
                        signalMessage = msg;
                        break;

                    case CHATROOM_CREATE_RESPONSE:
                    case CHATROOM_DELETE_RESPONSE:
                        signalMessage = msg;
                        break;

                    case CHATROOM_JOIN_RESPONSE:
                    case CHATROOM_LEAVE_RESPONSE:
                        signalMessage = msg;
                        break;

                    case LOGOUT_RESPONSE:
                        signalMessage = msg;
                        keepAlive = false;
                        break;

                    case ERROR_SERVER:
                        //TODO: HANDLE ERROR_SERVER_MESSAGES
                        signalMessage = msg;
                        break;
                }

            }
        } catch (EOFException e) {
            JOptionPane.showMessageDialog(this.gui.getMainWindow(), "Logged of due to timeout.", "Timeout logout", JOptionPane.ERROR_MESSAGE);
            this.gui.getMainWindow().dispose();
            System.exit(1);
        }

    }

    @Override
    public void run() {
        try {
            handleClient();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }
}
