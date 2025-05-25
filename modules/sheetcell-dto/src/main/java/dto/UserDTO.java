package dto;

/**
 * Data Transfer Object for User information
 * Contains user data needed for authentication and authorization
 */
public class UserDTO {
    private String username;
    private String sessionId;
    private long loginTime;

    // Default constructor for serialization frameworks
    public UserDTO() {
    }

    // Full constructor
    public UserDTO(String username, String sessionId, long loginTime) {
        this.username = username;
        this.sessionId = sessionId;
        this.loginTime = loginTime;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }
} 