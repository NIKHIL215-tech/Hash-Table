import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HashTable {

    static class Event {
        String url;
        String userId;
        String source;

        Event(String url, String userId, String source) {
            this.url = url;
            this.userId = userId;
            this.source = source;
        }
    }

    private ConcurrentHashMap<String, Integer> pageViews;
    private ConcurrentHashMap<String, Set<String>> uniqueVisitors;
    private ConcurrentHashMap<String, Integer> trafficSourceCount;

    public HashTable() {
        pageViews = new ConcurrentHashMap<>();
        uniqueVisitors = new ConcurrentHashMap<>();
        trafficSourceCount = new ConcurrentHashMap<>();
    }

    public void processEvent(Event event) {
        pageViews.merge(event.url, 1, Integer::sum);

        uniqueVisitors.computeIfAbsent(event.url, k -> ConcurrentHashMap.newKeySet())
                .add(event.userId);

        trafficSourceCount.merge(event.source, 1, Integer::sum);
    }

    public List<Map.Entry<String, Integer>> getTopPages(int k) {
        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {
            pq.offer(entry);
            if (pq.size() > k) {
                pq.poll();
            }
        }

        List<Map.Entry<String, Integer>> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            result.add(pq.poll());
        }

        Collections.reverse(result);
        return result;
    }

    public void getDashboard() {
        List<Map.Entry<String, Integer>> topPages = getTopPages(10);

        System.out.println("Top Pages:");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : topPages) {
            String url = entry.getKey();
            int views = entry.getValue();
            int unique = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();

            System.out.println(rank + ". " + url + " - " + views + " views (" + unique + " unique)");
            rank++;
        }

        System.out.println("\nTraffic Sources:");
        for (Map.Entry<String, Integer> entry : trafficSourceCount.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        HashTable system = new HashTable();

        system.processEvent(new Event("/article/breaking-news", "user_123", "google"));
        system.processEvent(new Event("/article/breaking-news", "user_456", "facebook"));
        system.processEvent(new Event("/sports/championship", "user_789", "direct"));
        system.processEvent(new Event("/article/breaking-news", "user_123", "google"));
        system.processEvent(new Event("/sports/championship", "user_101", "google"));

        for (int i = 0; i < 1000; i++) {
            system.processEvent(new Event("/article/breaking-news", "user_" + i, "google"));
        }

        Thread dashboardUpdater = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    System.out.println("\n--- DASHBOARD UPDATE ---");
                    system.getDashboard();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        dashboardUpdater.setDaemon(true);
        dashboardUpdater.start();

        Thread.sleep(6000);
    }
}