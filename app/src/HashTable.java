import java.util.*;

public class HashTable {

    enum Status {
        EMPTY, OCCUPIED, DELETED
    }

    class ParkingSpot {
        String licensePlate;
        long entryTime;
        Status status;

        ParkingSpot() {
            this.status = Status.EMPTY;
        }
    }

    private ParkingSpot[] table;
    private int capacity;
    private int size;

    private int totalProbes = 0;
    private int totalOperations = 0;

    private Map<Integer, Integer> hourlyCount;

    public HashTable(int capacity) {
        this.capacity = capacity;
        this.table = new ParkingSpot[capacity];
        this.size = 0;
        this.hourlyCount = new HashMap<>();

        for (int i = 0; i < capacity; i++) {
            table[i] = new ParkingSpot();
        }
    }

    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    public void parkVehicle(String licensePlate) {
        int index = hash(licensePlate);
        int probes = 0;

        for (int i = 0; i < capacity; i++) {
            int newIndex = (index + i) % capacity;

            if (table[newIndex].status == Status.EMPTY || table[newIndex].status == Status.DELETED) {
                table[newIndex].licensePlate = licensePlate;
                table[newIndex].entryTime = System.currentTimeMillis();
                table[newIndex].status = Status.OCCUPIED;

                size++;
                totalProbes += probes;
                totalOperations++;

                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                hourlyCount.put(hour, hourlyCount.getOrDefault(hour, 0) + 1);

                System.out.println("Assigned spot #" + newIndex + " (" + probes + " probes)");
                return;
            }
            probes++;
        }

        System.out.println("Parking Full");
    }

    public void exitVehicle(String licensePlate) {
        int index = hash(licensePlate);

        for (int i = 0; i < capacity; i++) {
            int newIndex = (index + i) % capacity;

            if (table[newIndex].status == Status.EMPTY) break;

            if (table[newIndex].status == Status.OCCUPIED &&
                    table[newIndex].licensePlate.equals(licensePlate)) {

                long durationMs = System.currentTimeMillis() - table[newIndex].entryTime;
                double hours = durationMs / (1000.0 * 60 * 60);
                double fee = hours * 5;

                table[newIndex].status = Status.DELETED;
                size--;

                System.out.println("Spot #" + newIndex + " freed, Duration: "
                        + String.format("%.2f", hours) + "h, Fee: $" + String.format("%.2f", fee));
                return;
            }
        }

        System.out.println("Vehicle not found");
    }

    public int findNearestAvailableSpot() {
        for (int i = 0; i < capacity; i++) {
            if (table[i].status == Status.EMPTY || table[i].status == Status.DELETED) {
                return i;
            }
        }
        return -1;
    }

    public void getStatistics() {
        double occupancy = (size * 100.0) / capacity;
        double avgProbes = totalOperations == 0 ? 0 : (double) totalProbes / totalOperations;

        int peakHour = -1, max = 0;
        for (Map.Entry<Integer, Integer> e : hourlyCount.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                peakHour = e.getKey();
            }
        }

        System.out.println("Occupancy: " + String.format("%.2f", occupancy) + "%");
        System.out.println("Avg Probes: " + String.format("%.2f", avgProbes));
        System.out.println("Peak Hour: " + peakHour + ":00 - " + (peakHour + 1) + ":00");
    }

    public static void main(String[] args) throws InterruptedException {
        HashTable parking = new HashTable(500);

        parking.parkVehicle("ABC-1234");
        parking.parkVehicle("ABC-1235");
        parking.parkVehicle("XYZ-9999");

        Thread.sleep(2000);

        parking.exitVehicle("ABC-1234");

        parking.getStatistics();
    }
}