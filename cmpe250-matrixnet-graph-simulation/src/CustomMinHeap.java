import java.util.ArrayList;

// Optimized Min-Heap for Dijkstra's algorithm
public class CustomMinHeap {
    private RouteNode[] heap;
    private int size;
    private static final int INITIAL_CAPACITY = 1000;

    // Buffers for zero-allocation path string comparison
    private static Host[] buffer1 = new Host[1000];
    private static Host[] buffer2 = new Host[1000];

    // Factory method for RouteNodes
    public static RouteNode getRouteNode(Host currentHost, double totalLatency, int hops, PathNode path) {
        return new RouteNode(currentHost, totalLatency, hops, path);
    }

    // Factory method for PathNodes
    public static PathNode getPathNode(Host host, PathNode prev) {
        return new PathNode(host, prev);
    }
    // ----------------------

    // Initializes the heap
    public CustomMinHeap() {
        this.heap = new RouteNode[INITIAL_CAPACITY];
        this.size = 0;
    }

    // Adds a node to the heap
    public void push(RouteNode node) {
        if (size == heap.length) {
            resize();
        }
        heap[size] = node;
        siftUp(size);
        size++;
    }

    // Doubles heap capacity
    private void resize() {
        RouteNode[] newHeap = new RouteNode[heap.length * 2];
        System.arraycopy(heap, 0, newHeap, 0, size);
        heap = newHeap;
    }

    // Ensures comparison buffers are large enough
    private static void ensureBufferCapacity(int capacity) {
        if (buffer1.length < capacity) {
            int newCap = Math.max(buffer1.length * 2, capacity);
            buffer1 = new Host[newCap];
            buffer2 = new Host[newCap];
        }
    }

    // Removes and returns the minimum element
    public RouteNode pop() {
        if (size == 0)
            return null;
        RouteNode root = heap[0];
        size--;
        RouteNode last = heap[size];
        heap[size] = null; // Clear reference

        if (size > 0) {
            heap[0] = last;
            siftDown(0);
        }
        return root;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    // Restores heap property upwards
    private void siftUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            RouteNode current = heap[index];
            RouteNode parent = heap[parentIndex];

            if (current.compareTo(parent) < 0) {
                heap[index] = parent;
                heap[parentIndex] = current;
                index = parentIndex;
            } else {
                break;
            }
        }
    }

    // Restores heap property downwards
    private void siftDown(int index) {
        while (true) {
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;
            int smallest = index;

            if (leftChild < size && heap[leftChild].compareTo(heap[smallest]) < 0) {
                smallest = leftChild;
            }
            if (rightChild < size && heap[rightChild].compareTo(heap[smallest]) < 0) {
                smallest = rightChild;
            }

            if (smallest != index) {
                RouteNode temp = heap[index];
                heap[index] = heap[smallest];
                heap[smallest] = temp;
                index = smallest;
            } else {
                break;
            }
        }
    }

    // Linked list node for efficient path storage
    public static class PathNode {
        public Host host;
        public PathNode prev;

        public PathNode(Host host, PathNode prev) {
            this.host = host;
            this.prev = prev;
        }

        // Reconstructs the full path list
        public ArrayList<Host> toList() {
            return toList(10); // Default capacity
        }

        public ArrayList<Host> toList(int capacity) {
            ArrayList<Host> list = new ArrayList<>(capacity);
            PathNode curr = this;
            while (curr != null) {
                list.add(curr.host);
                curr = curr.prev;
            }
            // Reverse to get start->end order
            ArrayList<Host> reversed = new ArrayList<>(list.size());
            for (int i = list.size() - 1; i >= 0; i--) {
                reversed.add(list.get(i));
            }
            return reversed;
        }
    }

    // Node representing a route in the search
    public static class RouteNode implements Comparable<RouteNode> {
        public Host currentHost;
        public double totalLatency;
        public int hops;
        public PathNode path; // Tail of the path
        public boolean valid = true; // Logic for lazy deletion

        public RouteNode(Host currentHost, double totalLatency, int hops, PathNode path) {
            this.currentHost = currentHost;
            this.totalLatency = totalLatency;
            this.hops = hops;
            this.path = path;
        }

        @Override
        public int compareTo(RouteNode other) {

            // 1. Prioritize lower latency
            if (this.totalLatency != other.totalLatency) {
                return Double.compare(this.totalLatency, other.totalLatency);
            }
            // 2. Prioritize fewer hops
            if (this.hops != other.hops) {
                return Integer.compare(this.hops, other.hops);
            }
            // 3. Break ties lexicographically
            return comparePaths(this, other);
        }

        private int comparePaths(RouteNode n1, RouteNode n2) {
            return CustomMinHeap.comparePath(n1.path, n2.path, n1.hops + 1);
        }
    }

    // Helper to compare a path against a candidate extension (for optimization)
    public static int comparePath(PathNode existingPath, Host finalHost, PathNode strictPrevPath, int length) {
        if (length <= 1)
            return 0; // Identical end

        return comparePath(existingPath.prev, strictPrevPath, length - 1);
    }

    // Compares two paths lexicographically without object allocation
    public static int comparePath(PathNode p1, PathNode p2, int length) {
        if (p1 != null && p2 != null && p1.prev == p2.prev) {
            return p1.host.id.compareTo(p2.host.id);
        }

        ensureBufferCapacity(length);

        // Fill buffer1
        PathNode c1 = p1;
        for (int i = length - 1; i >= 0; i--) {
            buffer1[i] = c1.host;
            c1 = c1.prev;
        }

        // Fill buffer2
        PathNode c2 = p2;
        for (int i = length - 1; i >= 0; i--) {
            buffer2[i] = c2.host;
            c2 = c2.prev;
        }

        // Compare element-wise
        for (int i = 0; i < length; i++) {
            int cmp = buffer1[i].id.compareTo(buffer2[i].id);
            if (cmp != 0)
                return cmp;
        }
        return 0;
    }
}
