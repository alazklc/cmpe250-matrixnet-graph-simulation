import java.util.ArrayList;

// Represents a network node (Host)
public class Host implements Comparable<Host> {
    public ArrayList<Backdoor> backdoors; // Connections to other hosts
    public int visitedToken; // Token for efficient traversal tracking
    public ArrayList<CustomMinHeap.RouteNode> bestRoutes; // Caches optimal routes

    public String id;
    public int clearance;

    // Initializes a host with an ID and clearance level
    public Host(String id, int clearance) {
        this.id = id;
        this.clearance = clearance;
        this.backdoors = new ArrayList<>();
        this.visitedToken = 0;
        this.bestRoutes = new ArrayList<>();
    }

    // Orders hosts lexicographically by ID
    @Override
    public int compareTo(Host other) {
        return this.id.compareTo(other.id);
    }

    @Override
    public String toString() {
        return id;
    }

}
