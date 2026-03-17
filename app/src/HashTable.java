import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HashTable {

    private ConcurrentHashMap<String, Integer> inventory;
    private ConcurrentHashMap<String, Queue<Integer>> waitingList;

    public HashTable() {
        inventory = new ConcurrentHashMap<>();
        waitingList = new ConcurrentHashMap<>();
    }

    public void addProduct(String productId, int stock) {
        inventory.put(productId, stock);
        waitingList.put(productId, new LinkedList<>());
    }

    public int checkStock(String productId) {
        return inventory.getOrDefault(productId, 0);
    }

    public String purchaseItem(String productId, int userId) {
        synchronized (productId.intern()) {
            int stock = inventory.getOrDefault(productId, 0);

            if (stock > 0) {
                inventory.put(productId, stock - 1);
                return "Success, " + (stock - 1) + " units remaining";
            } else {
                Queue<Integer> queue = waitingList.get(productId);
                queue.add(userId);
                return "Added to waiting list, position #" + queue.size();
            }
        }
    }

    public void restock(String productId, int quantity) {
        synchronized (productId.intern()) {
            int stock = inventory.getOrDefault(productId, 0);
            stock += quantity;

            Queue<Integer> queue = waitingList.get(productId);

            while (stock > 0 && !queue.isEmpty()) {
                int userId = queue.poll();
                stock--;
                System.out.println("Allocated to waiting user: " + userId);
            }

            inventory.put(productId, stock);
        }
    }

    public static void main(String[] args) {
        HashTable system = new HashTable();

        system.addProduct("IPHONE15_256GB", 100);

        System.out.println(system.checkStock("IPHONE15_256GB"));

        System.out.println(system.purchaseItem("IPHONE15_256GB", 12345));
        System.out.println(system.purchaseItem("IPHONE15_256GB", 67890));

        for (int i = 0; i < 100; i++) {
            system.purchaseItem("IPHONE15_256GB", i);
        }

        System.out.println(system.purchaseItem("IPHONE15_256GB", 99999));

        system.restock("IPHONE15_256GB", 5);
    }
}