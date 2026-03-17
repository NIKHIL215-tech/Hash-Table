import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HashTable {

    class DNSEntry {
        String domain;
        String ipAddress;
        long expiryTime;

        DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private ConcurrentHashMap<String, DNSEntry> cache;
    private int maxSize;

    private long hits = 0;
    private long misses = 0;
    private long totalLookupTime = 0;
    private long totalRequests = 0;

    public HashTable(int maxSize) {
        this.maxSize = maxSize;

        cache = new ConcurrentHashMap<String, DNSEntry>(16, 0.75f, 1) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > HashTable.this.maxSize;
            }
        };

        startCleanupThread();
    }

    public String resolve(String domain) {
        long start = System.nanoTime();
        totalRequests++;

        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            hits++;
            totalLookupTime += (System.nanoTime() - start);
            return "Cache HIT → " + entry.ipAddress;
        }

        if (entry != null && entry.isExpired()) {
            cache.remove(domain);
        }

        misses++;

        String ip = queryUpstreamDNS(domain);
        cache.put(domain, new DNSEntry(domain, ip, 5));

        totalLookupTime += (System.nanoTime() - start);
        return "Cache MISS → " + ip;
    }

    private String queryUpstreamDNS(String domain) {
        return "192.168." + new Random().nextInt(255) + "." + new Random().nextInt(255);
    }

    public void startCleanupThread() {
        Thread cleaner = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                    for (String key : cache.keySet()) {
                        DNSEntry entry = cache.get(key);
                        if (entry != null && entry.isExpired()) {
                            cache.remove(key);
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        cleaner.setDaemon(true);
        cleaner.start();
    }

    public String getCacheStats() {
        double hitRate = totalRequests == 0 ? 0 : (hits * 100.0 / totalRequests);
        double avgTimeMs = totalRequests == 0 ? 0 : (totalLookupTime / 1_000_000.0 / totalRequests);

        return "Hit Rate: " + String.format("%.2f", hitRate) + "%, Avg Lookup Time: " + String.format("%.3f", avgTimeMs) + "ms";
    }

    public static void main(String[] args) throws InterruptedException {
        HashTable dns = new HashTable(5);

        System.out.println(dns.resolve("google.com"));
        System.out.println(dns.resolve("google.com"));

        Thread.sleep(6000);

        System.out.println(dns.resolve("google.com"));

        System.out.println(dns.getCacheStats());
    }
}