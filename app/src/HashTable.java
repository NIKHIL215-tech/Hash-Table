import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HashTable {

    private ConcurrentHashMap<String, Integer> usernameMap;
    private ConcurrentHashMap<String, Integer> attemptCount;

    public HashTable() {
        usernameMap = new ConcurrentHashMap<>();
        attemptCount = new ConcurrentHashMap<>();
    }

    public boolean checkAvailability(String username) {
        attemptCount.merge(username, 1, Integer::sum);
        return !usernameMap.containsKey(username);
    }

    public void registerUsername(String username, int userId) {
        usernameMap.put(username, userId);
    }

    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String candidate = username + i;
            if (!usernameMap.containsKey(candidate)) {
                suggestions.add(candidate);
            }
        }

        String modified = username.replace("_", ".");
        if (!usernameMap.containsKey(modified)) {
            suggestions.add(modified);
        }

        return suggestions;
    }

    public String getMostAttempted() {
        String maxUser = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : attemptCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxUser = entry.getKey();
            }
        }
        return maxUser;
    }

    public static void main(String[] args) {
        HashTable system = new HashTable();

        system.registerUsername("john_doe", 1);
        system.registerUsername("admin", 2);

        System.out.println(system.checkAvailability("john_doe"));
        System.out.println(system.checkAvailability("jane_smith"));

        System.out.println(system.suggestAlternatives("john_doe"));

        for (int i = 0; i < 10543; i++) {
            system.checkAvailability("admin");
        }

        System.out.println(system.getMostAttempted());
    }
}