package chatapp.client;

public enum NetMessageType
{
                    // server side
    LOGIN_RESPONSE,                 // logging: err or success
    LOGOUT_RESPONSE,
    CREATE_ACCOUNT_RESPONSE,        // creating account: err or success
    DELETE_ACCOUNT_RESPOSE,
    CHATROOM_CREATE_RESPONSE,
    CHATROOM_DELETE_RESPONSE,       // chatroom: created or deleted
    CHATROOM_JOIN_RESPONSE,
    CHATROOM_LEAVE_RESPONSE,
    ERROR_SERVER,                   // general error
    LASTMSG_RESPOSE,                // last message request



                    // client side
    LOGIN_REQUEST,                  // logging: err or success
    LOGOUT_REQUEST,
    CREATE_ACCOUNT_REQUEST,         // creating account: err or success
    DELETE_ACCOUNT_REQUEST,
    CHATROOM_CREATE_REQUEST,
    CHATROOM_DELETE_REQUEST,        // chatroom: created or deleted
    CHATROOM_JOIN_REQUEST,
    CHATROOM_LEAVE_REQUEST,
    ERROR_CLIENT,                   // general error
    LASTMSG_REQUEST,                // last message request
}