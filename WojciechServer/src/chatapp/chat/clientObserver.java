package chatapp.chat;

import chatapp.net.NetMessage;
import chatapp.net.chatMessage;

public interface clientObserver {
    void update(chatMessage msg);
    void info(NetMessage msg);
}
