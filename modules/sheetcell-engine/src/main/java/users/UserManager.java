package users;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private final ConcurrentHashMap<String, Object> usersMap;

    public UserManager() {
        usersMap = new ConcurrentHashMap<>();
    }

    public void addUser(String username) {
        usersMap.put(username, new Object());
    }

    public void removeUser(String username) {
        usersMap.remove(username);
    }

    public Set<String> getUsers() {
        return Collections.unmodifiableSet(usersMap.keySet());
    }

    public boolean isUserExists(String username) {
        return usersMap.containsKey(username);
    }
}
