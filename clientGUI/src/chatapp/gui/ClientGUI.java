package chatapp.gui;

import chatapp.logic.ChatLogic;
import chatapp.net.chatMessage;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;

public class ClientGUI {
    private static final int W_WIDTH = 600;
    private static final int W_HEIGHT = 500;
    private static final String WELCOMEFILE = "/home/kkinshiryuu/PROJECTS/WOJCIECH_LY/clientGUI/src/chatapp/gui/welcome.txt";

    private String loginUser;               // variable for window - show who is logged
    private String actualWindow;            // variable to switch input of user
    private int numberOfWindows = 0;        // variable to track the actual number of windows
    private HashMap<String, Integer> tabToIdx = new HashMap<>(); // Map to collect index of tab for certain chatroom window
    private ChatLogic chatLogic;

    //TODO: Change userNames class

    private JFrame mainWindow = new JFrame("Chat Application");

    private JMenuBar menuBar = new JMenuBar();
    private JMenu file = new JMenu("File");
    private JMenu edit = new JMenu("Edit");
    private JMenu user = new JMenu("User");

    private JMenuItem settings = new JMenuItem("Settings");

    private JMenuItem addChat = new JMenuItem("Add New Chat");
    private JMenuItem deleteChat = new JMenuItem("Delete Chat");
    private JMenuItem joinChat = new JMenuItem("Join Chat");
    private JMenuItem leaveChat = new JMenuItem("Leave Chat");

    private JMenuItem logout = new JMenuItem("Log Out");
    private JMenuItem preferences = new JMenuItem("Preferences");
    private JMenuItem exit = new JMenuItem("Exit");

    private JPanel abovePane = new JPanel();
    private JLabel nameLabel = new JLabel("UserName");

    private HashMap<String, JTextArea> chatMap = new HashMap<>();// Collections of chatRoom names and textAreas
    private HashMap<String, JScrollPane> scrollMap = new HashMap<>(); // Collection of chatRoom names and scrollPanes

    private JTabbedPane chatAreaPane = new JTabbedPane();

    private HashMap<String, Vector<String>> peopleInRoom = new HashMap<>();
    private JTextArea peopleMap = new JTextArea(30,10);
    private JScrollPane peopleScroolMap = new JScrollPane(peopleMap);

    private JTabbedPane roomPeople = new JTabbedPane();

    private JPanel bottomPane = new JPanel();
    private JTextField textField = new JTextField(40);
    private JButton sendButton = new JButton("Send");

    public ClientGUI(){
        initializeGUI();
    }

    public JFrame getMainWindow() {
        return mainWindow;
    }

    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
    }

    public String getActualWindow() {
        return actualWindow;
    }

    public HashMap<String, Vector<String>> getPeopleInRoom() {
        return peopleInRoom;
    }

    public HashMap<String, JTextArea> getChatMap() {
        return chatMap;
    }

    public void addUser(String chatRoom, String username){
        if(peopleInRoom.containsKey(chatRoom)){
            Vector<String> temp = peopleInRoom.get(chatRoom);
            if(!temp.contains(username)){
                peopleInRoom.get(chatRoom).addElement(username);
            }
        }
        if(actualWindow.equals(chatRoom)){
            refreshList(chatRoom);
        }
    }

    public void addUser(String chatRoom, Vector<String> users){
        if(peopleInRoom.containsKey(chatRoom)){
            Vector<String> temp = peopleInRoom.get(chatRoom);
            for(String username: users) {
                if (!temp.contains(username)) {
                    peopleInRoom.get(chatRoom).addElement(username);
                }
            }
        }
        if(actualWindow.equals(chatRoom)){
            refreshList(chatRoom);
        }
    }

    public void deleteUser(String chatRoom, String username){
        if(peopleInRoom.containsKey(chatRoom)){
            Vector<String> temp = peopleInRoom.get(chatRoom);
            if(temp.contains(username)){
                peopleInRoom.get(chatRoom).removeElement(username);
            }
        }
        if(actualWindow.equals(chatRoom)){
            refreshList(chatRoom);
        }
    }

    public void deleteUser(String chatRoom, Vector<String> users){
        if(peopleInRoom.containsKey(chatRoom)){
            Vector<String> temp = peopleInRoom.get(chatRoom);
            for(String username: users) {
                if (temp.contains(username)) {
                    peopleInRoom.get(chatRoom).removeElement(username);
                }
            }
        }
        if(actualWindow.equals(chatRoom)){
            refreshList(chatRoom);
        }
    }

    public void refreshList(String chatRoom){
        peopleMap.setText("");
        Vector<String> temp = peopleInRoom.get(chatRoom);
        for(String username: temp){
            peopleMap.append(username + "\n");
        }
    }

    public void addTab(String chatRoomName){           // Method to create and maintain new tabs
        chatMap.put(chatRoomName, new JTextArea(30,30));
        chatMap.get(chatRoomName).setEditable(false);
        scrollMap.put(chatRoomName, new JScrollPane(chatMap.get(chatRoomName)));
        tabToIdx.put(chatRoomName, numberOfWindows);
        numberOfWindows++;
        chatAreaPane.add(chatRoomName, scrollMap.get(chatRoomName));
    }

    public void deleteTab(String chatRoomName){        // Method to delete and remove tab
        try {
            chatAreaPane.remove(scrollMap.get(chatRoomName));
            scrollMap.remove(chatRoomName);
            chatMap.remove(chatRoomName);
            peopleInRoom.remove(chatRoomName);
            numberOfWindows--;
        } catch (Exception e){
            chatLogic.getLOGGER().log(Level.SEVERE, e.toString(), e);
        }
    }

    private void initializeGUI(){
        chatLogic = new ChatLogic(this);
        boolean isLogged = false;
        while(!isLogged){
            isLogged = chatLogic.logInWindow();
        }

        actualWindow = "Welcome!";

        chatMap.put(actualWindow, new JTextArea(30,30));
        scrollMap.put(actualWindow, new JScrollPane(chatMap.get(actualWindow)));
        tabToIdx.put(actualWindow, numberOfWindows);
        peopleInRoom.put(actualWindow, new Vector<>());

        try {
            BufferedReader br = new BufferedReader(new FileReader(WELCOMEFILE));
            String line;
            while((line = br.readLine()) != null){
                chatMap.get(actualWindow).append(line + "\n");
            }
            br.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        peopleInRoom.get(actualWindow).addElement("In this window");
        peopleInRoom.get(actualWindow).addElement("you will have");
        peopleInRoom.get(actualWindow).addElement("users logged in room.");
        peopleInRoom.get(actualWindow).addElement("They can log in,");
        peopleInRoom.get(actualWindow).addElement("log out, remove the room,");
        peopleInRoom.get(actualWindow).addElement("join the room");

        refreshList(actualWindow);

        numberOfWindows++;

        mainWindow.setLayout(new BorderLayout(10,10));
        mainWindow.setSize(W_WIDTH,W_HEIGHT);

        chatAreaPane.add(actualWindow, scrollMap.get(actualWindow));
        roomPeople.add("People in Room", peopleScroolMap);

        bottomPane.add(textField);
        bottomPane.add(sendButton);

        nameLabel.setText(loginUser);
        abovePane.add(nameLabel);

        mainWindow.add(abovePane, BorderLayout.NORTH);
        mainWindow.add(chatAreaPane, BorderLayout.CENTER);
        mainWindow.add(roomPeople, BorderLayout.WEST);
        mainWindow.add(bottomPane, BorderLayout.SOUTH);

        file.add(settings);
        edit.add(addChat);
        edit.add(deleteChat);
        edit.add(joinChat);
        edit.add(leaveChat);
        user.add(preferences);
        user.add(logout);
        user.add(exit);

        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(user);

        mainWindow.setJMenuBar(menuBar);
        mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainWindow.setVisible(true);

        chatMap.get(actualWindow).setEditable(false);
        peopleMap.setEditable(false);
        textField.setEditable(true);

        chatAreaPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JTabbedPane sourceTab = (JTabbedPane) e.getSource();
                int index = sourceTab.getSelectedIndex();
                String chatname = sourceTab.getTitleAt(index);
                actualWindow = chatname;
                refreshList(actualWindow);
                if(actualWindow.equals("Welcome!")){
                    textField.setEditable(false);
                    sendButton.setEnabled(false);
                } else {
                    textField.setEditable(true);
                    sendButton.setEnabled(true);
                }
            }
        });

        roomPeople.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JTabbedPane sourceTab = (JTabbedPane) e.getSource();
                int index = sourceTab.getSelectedIndex();
                String chatname = sourceTab.getTitleAt(index);
            }
        });

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = textField.getText();
                chatMessage chatMsg = new chatMessage(actualWindow, loginUser, message);
                chatLogic.sendChatMessage(chatMsg);
                textField.setText("");
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = textField.getText();
                chatMessage chatMsg = new chatMessage(actualWindow, loginUser, message);
                chatLogic.sendChatMessage(chatMsg);
                textField.setText("");
            }
        });

        addChat.addActionListener(new ActionListener() { // Listener for addChatB Button - works
            @Override
            public void actionPerformed(ActionEvent e) {
                chatLogic.createChatRoom();
            }
        });

        deleteChat.addActionListener(new ActionListener() {     // Listener for deleteChat button - works
            @Override
            public void actionPerformed(ActionEvent e) {
                chatLogic.deleteChatRoom();
            }
        });

        joinChat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               chatLogic.joinChatRoom();
            }
        });

        leaveChat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chatLogic.leaveChatRoom();
            }
        });

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chatLogic.exitWindow();
            }
        });

        mainWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                chatLogic.exitWindow();
            }
        });


        chatLogic.start();
    }


    public static void main(String[] args) {
        new ClientGUI();
    }
}
