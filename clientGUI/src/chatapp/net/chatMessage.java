package chatapp.net;

import java.io.Serializable;

public class chatMessage implements Serializable
{
    private static final long serialVersionUID = 1235L;
    private String chatRoom;
    private String clientName,messageContent;

    public chatMessage(String chatRoom, String clientName, String messageContent)
    {
        this.chatRoom = chatRoom;
        this.clientName = clientName;
        this.messageContent = messageContent;
    }

    public String getChatRoom() {
        return chatRoom;
    }

    public String getClientName()
    {
        return clientName;
    }

    public String getMessageContent()
    {
        return messageContent;
    }
}
