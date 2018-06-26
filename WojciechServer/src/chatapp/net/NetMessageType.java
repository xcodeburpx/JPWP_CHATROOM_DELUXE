package chatapp.net;

public enum NetMessageType
{
                    // server side
    CONNECTION_RESPONSE,
    LOGIN_RESPONSE,                 // logging: err or success
    LOGOUT_RESPONSE,
    CREATE_ACCOUNT_RESPONSE,        // creating account: err or success
    DELETE_ACCOUNT_RESPOSE,
    CHATROOM_EXISTS_REQUEST,
    CHATROOM_CREATE_RESPONSE,
    CHATROOM_DELETE_RESPONSE,       // chatroom: created or deleted
    CHATROOM_JOIN_RESPONSE,
    CHATROOM_LEAVE_RESPONSE,
    USER_ADDED,
    USER_DELETED,
    ERROR_SERVER,                   // general error




                    // client side
    CONNECTION_REQUEST,
    LOGIN_REQUEST,                  // logging: err or success
    LOGOUT_REQUEST,
    CREATE_ACCOUNT_REQUEST,         // creating account: err or success
    DELETE_ACCOUNT_REQUEST,
    CHATROOM_DOES_NOT_EXIST_RESPONSE,
    CHATROOM_EXISTS_RESPONSE,
    CHATROOM_CREATE_REQUEST,
    CHATROOM_DELETE_REQUEST,        // chatroom: created or deleted
    CHATROOM_JOIN_REQUEST,
    CHATROOM_LEAVE_REQUEST,
    ERROR_CLIENT,                   // general error
    MSG                             // message sent by user
}