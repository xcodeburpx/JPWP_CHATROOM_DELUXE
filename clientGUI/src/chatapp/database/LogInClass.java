package chatapp.database;



public class LogInClass {
    private String ipAddress;
    private UserLogInClass userData;

    public LogInClass( UserLogInClass userData, String ipAddress) {
        this.ipAddress = ipAddress;
        this.userData = userData;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public UserLogInClass getUserData() {
        return userData;
    }
}
