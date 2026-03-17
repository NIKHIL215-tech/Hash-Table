import java.util.concurrent.ConcurrentHashMap;

public class HashTable {

    class TokenBucket {
        private int tokens;
        private final int maxTokens;
        private final double refillRatePerSec;
        private long lastRefillTime;

        public TokenBucket(int maxTokens, double refillRatePerSec) {
            this.maxTokens = maxTokens;
            this.refillRatePerSec = refillRatePerSec;
            this.tokens = maxTokens;
            this.lastRefillTime = System.currentTimeMillis();
        }

        private void refill() {
            long now = System.currentTimeMillis();
            double seconds = (now - lastRefillTime) / 1000.0;

            int tokensToAdd = (int) (seconds * refillRatePerSec);
            if (tokensToAdd > 0) {
                tokens = Math.min(maxTokens, tokens + tokensToAdd);
                lastRefillTime = now;
            }
        }

        public synchronized boolean allowRequest() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        public synchronized int getRemainingTokens() {
            refill();
            return tokens;
        }

        public synchronized long getRetryAfterSeconds() {
            if (tokens > 0) return 0;
            return (long) Math.ceil(1.0 / refillRatePerSec);
        }
    }

    private ConcurrentHashMap<String, TokenBucket> clientBuckets;

    private static final int MAX_REQUESTS = 1000;
    private static final double REFILL_RATE = MAX_REQUESTS / 3600.0; // per second

    public HashTable() {
        clientBuckets = new ConcurrentHashMap<>();
    }

    private TokenBucket getBucket(String clientId) {
        return clientBuckets.computeIfAbsent(clientId,
                k -> new TokenBucket(MAX_REQUESTS, REFILL_RATE));
    }

    public String checkRateLimit(String clientId) {
        TokenBucket bucket = getBucket(clientId);

        if (bucket.allowRequest()) {
            return "Allowed (" + bucket.getRemainingTokens() + " requests remaining)";
        } else {
            return "Denied (0 requests remaining, retry after "
                    + bucket.getRetryAfterSeconds() + "s)";
        }
    }

    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = getBucket(clientId);
        int remaining = bucket.getRemainingTokens();
        int used = MAX_REQUESTS - remaining;
        long resetTime = (System.currentTimeMillis() / 1000) + bucket.getRetryAfterSeconds();

        return "{used: " + used +
                ", limit: " + MAX_REQUESTS +
                ", reset: " + resetTime + "}";
    }

    public static void main(String[] args) {
        HashTable limiter = new HashTable();

        String client = "abc123";

        for (int i = 0; i < 1005; i++) {
            System.out.println(limiter.checkRateLimit(client));
        }

        System.out.println(limiter.getRateLimitStatus(client));
    }
}