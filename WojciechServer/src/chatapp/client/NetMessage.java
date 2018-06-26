package chatapp.client;

import java.io.Serializable;

public class NetMessage  implements Serializable{
    private static final long serialVersionUID = 1234L;
    private NetMessageType type;
    private Object content;

    public NetMessage(NetMessageType type, Object content)
    {
        this.type = type;
        this.content = content;
    }

    public NetMessageType getType()
    {
        return type;
    }

    public Object getContent()
    {
        return content;
    }
}
