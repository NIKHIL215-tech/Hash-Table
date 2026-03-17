import java.util.*;

public class HashTable {

    private static final int N = 5; // n-gram size

    // n-gram -> set of document IDs
    private HashMap<String, Set<String>> index;

    // document -> list of n-grams
    private HashMap<String, List<String>> documentNGrams;

    public HashTable() {
        index = new HashMap<>();
        documentNGrams = new HashMap<>();
    }

    // Preprocess and store document
    public void addDocument(String docId, String content) {
        List<String> ngrams = generateNGrams(content);
        documentNGrams.put(docId, ngrams);

        for (String gram : ngrams) {
            index.computeIfAbsent(gram, k -> new HashSet<>()).add(docId);
        }
    }

    // Generate n-grams
    private List<String> generateNGrams(String text) {
        List<String> result = new ArrayList<>();
        String[] words = text.toLowerCase().split("\\W+");

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder gram = new StringBuilder();
            for (int j = 0; j < N; j++) {
                gram.append(words[i + j]).append(" ");
            }
            result.add(gram.toString().trim());
        }
        return result;
    }

    // Analyze a document for plagiarism
    public void analyzeDocument(String docId, String content) {
        List<String> ngrams = generateNGrams(content);
        System.out.println("Extracted " + ngrams.size() + " n-grams");

        HashMap<String, Integer> matchCount = new HashMap<>();

        for (String gram : ngrams) {
            if (index.containsKey(gram)) {
                for (String matchedDoc : index.get(gram)) {
                    matchCount.put(matchedDoc,
                            matchCount.getOrDefault(matchedDoc, 0) + 1);
                }
            }
        }

        for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
            String otherDoc = entry.getKey();
            int matches = entry.getValue();

            int total = ngrams.size();
            double similarity = (matches * 100.0) / total;

            System.out.println("Found " + matches + " matching n-grams with \"" + otherDoc + "\"");
            System.out.println("Similarity: " + String.format("%.2f", similarity) + "% "
                    + (similarity > 50 ? "(PLAGIARISM DETECTED)" :
                    similarity > 15 ? "(suspicious)" : "(low)"));
        }
    }

    public static void main(String[] args) {
        HashTable system = new HashTable();

        String doc1 = "Artificial intelligence is transforming the world with advanced machine learning techniques";
        String doc2 = "Machine learning techniques are transforming the world of artificial intelligence rapidly";

        system.addDocument("essay_092.txt", doc1);
        system.addDocument("essay_089.txt", doc2);

        String newDoc = "Artificial intelligence is transforming the world with machine learning techniques rapidly";

        system.analyzeDocument("essay_123.txt", newDoc);
    }
}