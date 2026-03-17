import java.util.*;

public class HashTable {

    static class Transaction {
        int id;
        int amount;
        String merchant;
        String account;
        long time; // epoch milliseconds

        Transaction(int id, int amount, String merchant, String account, long time) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            this.time = time;
        }
    }

    // -------------------- TWO SUM --------------------
    public List<int[]> findTwoSum(List<Transaction> transactions, int target) {
        Map<Integer, Transaction> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                result.add(new int[]{map.get(complement).id, t.id});
            }

            map.put(t.amount, t);
        }
        return result;
    }

    // -------------------- TWO SUM WITH TIME WINDOW --------------------
    public List<int[]> findTwoSumWithWindow(List<Transaction> transactions, int target, long windowMillis) {
        List<int[]> result = new ArrayList<>();
        Map<Integer, List<Transaction>> map = new HashMap<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                for (Transaction prev : map.get(complement)) {
                    if (Math.abs(t.time - prev.time) <= windowMillis) {
                        result.add(new int[]{prev.id, t.id});
                    }
                }
            }

            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }
        return result;
    }

    // -------------------- K SUM --------------------
    public List<List<Integer>> findKSum(List<Transaction> transactions, int k, int target) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(transactions, k, target, 0, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(List<Transaction> transactions, int k, int target, int start,
                           List<Integer> current, List<List<Integer>> result) {

        if (k == 0 && target == 0) {
            result.add(new ArrayList<>(current));
            return;
        }

        if (k == 0 || target < 0) return;

        for (int i = start; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);

            current.add(t.id);
            backtrack(transactions, k - 1, target - t.amount, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    // -------------------- DUPLICATE DETECTION --------------------
    public List<String> detectDuplicates(List<Transaction> transactions) {
        Map<String, Set<String>> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {
            String key = t.amount + "_" + t.merchant;

            map.computeIfAbsent(key, k -> new HashSet<>()).add(t.account);
        }

        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.add("Duplicate → " + entry.getKey() + " accounts: " + entry.getValue());
            }
        }

        return result;
    }

    // -------------------- MAIN --------------------
    public static void main(String[] args) {
        HashTable system = new HashTable();

        List<Transaction> transactions = new ArrayList<>();

        long now = System.currentTimeMillis();

        transactions.add(new Transaction(1, 500, "Store A", "acc1", now));
        transactions.add(new Transaction(2, 300, "Store B", "acc2", now + 1000));
        transactions.add(new Transaction(3, 200, "Store C", "acc3", now + 2000));
        transactions.add(new Transaction(4, 500, "Store A", "acc4", now + 3000));

        // Two Sum
        List<int[]> pairs = system.findTwoSum(transactions, 500);
        for (int[] p : pairs) {
            System.out.println("TwoSum Pair: " + p[0] + ", " + p[1]);
        }

        // Two Sum with 1 hour window
        List<int[]> windowPairs = system.findTwoSumWithWindow(transactions, 500, 3600_000);
        for (int[] p : windowPairs) {
            System.out.println("Window Pair: " + p[0] + ", " + p[1]);
        }

        // K Sum
        List<List<Integer>> ksum = system.findKSum(transactions, 3, 1000);
        for (List<Integer> list : ksum) {
            System.out.println("KSum: " + list);
        }

        // Duplicate Detection
        List<String> duplicates = system.detectDuplicates(transactions);
        for (String d : duplicates) {
            System.out.println(d);
        }
    }
}