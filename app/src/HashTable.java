import java.util.*;

public class HashTable {

    static class Video {
        String videoId;
        String data;

        Video(String videoId, String data) {
            this.videoId = videoId;
            this.data = data;
        }
    }

    // LRU Cache using LinkedHashMap
    class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private int capacity;

        public LRUCache(int capacity) {
            super(capacity, 0.75f, true);
            this.capacity = capacity;
        }

        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    private LRUCache<String, Video> L1;
    private LRUCache<String, Video> L2;
    private HashMap<String, Video> L3;

    private HashMap<String, Integer> accessCount;

    private int l1Hits = 0, l2Hits = 0, l3Hits = 0, totalRequests = 0;

    public HashTable() {
        L1 = new LRUCache<>(10000);
        L2 = new LRUCache<>(100000);
        L3 = new HashMap<>();
        accessCount = new HashMap<>();

        // Preload L3 database
        for (int i = 1; i <= 200000; i++) {
            String id = "video_" + i;
            L3.put(id, new Video(id, "VideoData_" + i));
        }
    }

    public Video getVideo(String videoId) {
        totalRequests++;

        // L1 Check
        if (L1.containsKey(videoId)) {
            l1Hits++;
            return L1.get(videoId);
        }

        // L2 Check
        if (L2.containsKey(videoId)) {
            l2Hits++;
            Video v = L2.get(videoId);

            promoteToL1(videoId, v);
            return v;
        }

        // L3 Check
        Video v = L3.get(videoId);
        if (v != null) {
            l3Hits++;

            L2.put(videoId, v);
            accessCount.put(videoId, 1);

            return v;
        }

        return null;
    }

    private void promoteToL1(String videoId, Video v) {
        int count = accessCount.getOrDefault(videoId, 0) + 1;
        accessCount.put(videoId, count);

        if (count >= 2) {
            L1.put(videoId, v);
        }
    }

    public void updateVideo(String videoId, String newData) {
        Video v = new Video(videoId, newData);
        L3.put(videoId, v);

        L1.remove(videoId);
        L2.remove(videoId);
        accessCount.remove(videoId);
    }

    public void getStatistics() {
        double l1Rate = totalRequests == 0 ? 0 : (l1Hits * 100.0 / totalRequests);
        double l2Rate = totalRequests == 0 ? 0 : (l2Hits * 100.0 / totalRequests);
        double l3Rate = totalRequests == 0 ? 0 : (l3Hits * 100.0 / totalRequests);

        System.out.println("L1: Hit Rate " + String.format("%.2f", l1Rate) + "%, Avg Time: 0.5ms");
        System.out.println("L2: Hit Rate " + String.format("%.2f", l2Rate) + "%, Avg Time: 5ms");
        System.out.println("L3: Hit Rate " + String.format("%.2f", l3Rate) + "%, Avg Time: 150ms");

        double overall = ((l1Hits + l2Hits + l3Hits) * 100.0) / totalRequests;
        System.out.println("Overall Hit Rate: " + String.format("%.2f", overall) + "%");
    }

    public static void main(String[] args) {
        HashTable cache = new HashTable();

        cache.getVideo("video_123");
        cache.getVideo("video_123");

        cache.getVideo("video_99999");
        cache.getVideo("video_99999");

        cache.getVideo("video_150000");

        cache.getStatistics();

        cache.updateVideo("video_123", "UpdatedData");
        cache.getVideo("video_123");

        cache.getStatistics();
    }
}