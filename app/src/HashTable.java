import java.util.*;

public class HashTable {

    class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        Map<String, Integer> prefixMap = new HashMap<>();
        boolean isEnd;
    }

    private TrieNode root;
    private HashMap<String, Integer> frequencyMap;

    public HashTable() {
        root = new TrieNode();
        frequencyMap = new HashMap<>();
    }

    public void insert(String query) {
        frequencyMap.put(query, frequencyMap.getOrDefault(query, 0) + 1);
        int freq = frequencyMap.get(query);

        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);

            node.prefixMap.put(query, freq);
        }
        node.isEnd = true;
    }

    public List<Map.Entry<String, Integer>> search(String prefix) {
        TrieNode node = root;

        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return new ArrayList<>();
            }
            node = node.children.get(c);
        }

        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<String, Integer> entry : node.prefixMap.entrySet()) {
            pq.offer(entry);
            if (pq.size() > 10) {
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

    public void updateFrequency(String query) {
        insert(query);
    }

    public List<String> suggestWithTypo(String prefix) {
        List<String> results = new ArrayList<>();

        if (search(prefix).size() > 0) {
            for (Map.Entry<String, Integer> e : search(prefix)) {
                results.add(e.getKey());
            }
            return results;
        }

        for (String query : frequencyMap.keySet()) {
            if (isCloseMatch(prefix, query)) {
                results.add(query);
            }
        }

        return results;
    }

    private boolean isCloseMatch(String a, String b) {
        int diff = Math.abs(a.length() - b.length());
        if (diff > 2) return false;

        int mismatches = 0;
        for (int i = 0; i < Math.min(a.length(), b.length()); i++) {
            if (a.charAt(i) != b.charAt(i)) mismatches++;
            if (mismatches > 2) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        HashTable system = new HashTable();

        system.insert("java tutorial");
        system.insert("javascript");
        system.insert("java download");
        system.insert("java tutorial");
        system.insert("java tutorial");
        system.insert("java 21 features");

        List<Map.Entry<String, Integer>> results = system.search("jav");

        int rank = 1;
        for (Map.Entry<String, Integer> entry : results) {
            System.out.println(rank + ". " + entry.getKey() + " (" + entry.getValue() + " searches)");
            rank++;
        }

        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");

        System.out.println("\nTypo Suggestions:");
        for (String s : system.suggestWithTypo("jva")) {
            System.out.println(s);
        }
    }
}