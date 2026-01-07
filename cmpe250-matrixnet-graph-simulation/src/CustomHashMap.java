import java.util.ArrayList;
import java.util.LinkedList;

// Custom hash map implementation for storing Hosts
public class CustomHashMap {
    private static final int INITIAL_CAPACITY = 10007; // Prime size to reduce collisions
    private ArrayList<LinkedList<Entry>> buckets;
    private int size;

    // Key-value pair entry
    private static class Entry {
        String key;
        Host value;

        Entry(String key, Host value) {
            this.key = key;
            this.value = value;
        }
    }

    // Initialize with default capacity
    public CustomHashMap() {
        buckets = new ArrayList<>(INITIAL_CAPACITY);
        for (int i = 0; i < INITIAL_CAPACITY; i++) {
            buckets.add(new LinkedList<>());
        }
        size = 0;
    }

    // Computes bucket index for a key
    private int getBucketIndex(String key, int capacity) {
        int hashCode = key.hashCode();
        int index = hashCode % capacity;
        return index < 0 ? index + capacity : index;
    }

    // Inserts or updates a value
    public void put(String key, Host value) {
        // Check load factor and resize if needed
        if (size >= buckets.size() * 0.75) {
            resize();
        }

        int index = getBucketIndex(key, buckets.size());
        LinkedList<Entry> bucket = buckets.get(index);

        for (Entry entry : bucket) {
            if (entry.key.equals(key)) {
                entry.value = value;
                return;
            }
        }

        bucket.add(new Entry(key, value));
        size++;
    }

    // Douglasses the capacity and rehashes entries
    private void resize() {
        int newCapacity = buckets.size() * 2;
        ArrayList<LinkedList<Entry>> newBuckets = new ArrayList<>(newCapacity);
        for (int i = 0; i < newCapacity; i++) {
            newBuckets.add(new LinkedList<>());
        }

        for (LinkedList<Entry> bucket : buckets) {
            for (Entry entry : bucket) {
                int index = getBucketIndex(entry.key, newCapacity);
                newBuckets.get(index).add(entry);
            }
        }
        buckets = newBuckets;
    }

    // Retrieves a value by key
    public Host get(String key) {
        int index = getBucketIndex(key, buckets.size());
        LinkedList<Entry> bucket = buckets.get(index);

        for (Entry entry : bucket) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }

        return null;
    }

    // Checks if a key exists
    public boolean containsKey(String key) {
        return get(key) != null;
    }

    // Returns all stored values
    public ArrayList<Host> values() {
        ArrayList<Host> allHosts = new ArrayList<>();
        for (LinkedList<Entry> bucket : buckets) {
            for (Entry entry : bucket) {
                allHosts.add(entry.value);
            }
        }
        return allHosts;
    }

    public int size() {
        return size;
    }
}
