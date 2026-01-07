import java.util.ArrayList;
import java.util.LinkedList;
import java.math.BigDecimal;
import java.math.RoundingMode;

// Main logic class for the network simulation
public class MatrixNet {
    private CustomHashMap hostMap; // Fast lookup for hosts
    private ArrayList<Host> hosts; // List of all hosts

    public MatrixNet() {
        this.hostMap = new CustomHashMap();
        this.hosts = new ArrayList<>();
    }

    // Creates a new host in the network
    public String spawnHost(String id, int clearance) {
        if (!id.matches("^[A-Z0-9_]+$")) {
            return "Some error occurred in spawn_host.";
        }
        if (hostMap.containsKey(id)) {
            return "Some error occurred in spawn_host.";
        }
        Host newHost = new Host(id, clearance);
        hostMap.put(id, newHost);
        hosts.add(newHost);
        return "Spawned host " + id + " with clearance level " + clearance + ".";
    }

    // Connects two hosts with a backdoor
    public String linkBackdoor(String id1, String id2, int latency, int bandwidth, int firewall) {
        Host h1 = hostMap.get(id1);
        Host h2 = hostMap.get(id2);

        if (h1 == null || h2 == null || id1.equals(id2)) {
            return "Some error occurred in link_backdoor.";
        }

        // Check if connection already exists
        for (Backdoor bd : h1.backdoors) {
            if (bd.getNeighbor(h1) == h2) {
                return "Some error occurred in link_backdoor.";
            }
        }

        Backdoor bd = new Backdoor(h1, h2, latency, bandwidth, firewall);
        h1.backdoors.add(bd);
        h2.backdoors.add(bd);

        return "Linked " + id1 + " <-> " + id2 + " with latency " + latency + "ms, bandwidth " + bandwidth
                + "Mbps, firewall " + firewall + ".";
    }

    // Toggles the sealed status of a connection
    public String sealBackdoor(String id1, String id2) {
        Host h1 = hostMap.get(id1);
        Host h2 = hostMap.get(id2);

        if (h1 == null || h2 == null) {
            return "Some error occurred in seal_backdoor.";
        }

        Backdoor target = null;
        for (Backdoor bd : h1.backdoors) {
            if (bd.getNeighbor(h1) == h2) {
                target = bd;
                break;
            }
        }

        if (target == null) {
            return "Some error occurred in seal_backdoor.";
        }

        if (!target.isSealed) {
            target.isSealed = true;
            return "Backdoor " + id1 + " <-> " + id2 + " sealed.";
        } else {
            target.isSealed = false;
            return "Backdoor " + id1 + " <-> " + id2 + " unsealed.";
        }
    }

    private int currentToken = 0; // Global token for traversal validity

    // Finds the optimal route between two hosts
    public String traceRoute(String id1, String id2, int minBandwidth, int lambda) {
        Host start = hostMap.get(id1);
        Host end = hostMap.get(id2);

        if (start == null || end == null) {
            return "Some error occurred in trace_route.";
        }

        if (id1.equals(id2)) {
            return "Optimal route " + id1 + " -> " + id2 + ": " + id1 + " (Latency = 0ms)";
        }

        // Initialize Dijkstra
        CustomMinHeap pq = new CustomMinHeap();

        // Clear previous route caches
        for (Host h : hosts) {
            h.bestRoutes.clear();
        }

        CustomMinHeap.PathNode startPath = CustomMinHeap.getPathNode(start, null);
        CustomMinHeap.RouteNode startNode = CustomMinHeap.getRouteNode(start, 0, 0, startPath);
        pq.push(startNode);
        start.bestRoutes.add(startNode);

        while (!pq.isEmpty()) {

            CustomMinHeap.RouteNode current = pq.pop();

            // Skip invalidated routes
            if (!current.valid) {
                continue;
            }

            Host u = current.currentHost;

            if (u == end) {
                return formatRoute(current, id1, id2);
            }

            for (Backdoor bd : u.backdoors) {

                if (bd.isSealed)
                    continue;
                if (bd.bandwidth < minBandwidth)
                    continue;

                Host v = bd.getNeighbor(u);

                if (u.clearance < bd.firewall)
                    continue;

                double newLatency;
                if (lambda == 0) {
                    newLatency = current.totalLatency + bd.latency;
                } else {
                    newLatency = current.totalLatency + bd.latency + (lambda * current.hops);
                }
                int newHops = current.hops + 1;

                // Pruning check: Is this new path dominated by an existing one?
                boolean strictlyDominated = false;
                for (int i = 0; i < v.bestRoutes.size(); i++) {
                    CustomMinHeap.RouteNode existing = v.bestRoutes.get(i);
                    // Check performance domination (Cost/Hops)
                    if (existing.totalLatency <= newLatency && existing.hops <= newHops) {
                        if (existing.totalLatency < newLatency || existing.hops < newHops) {
                            strictlyDominated = true;
                            break;
                        }
                        // Break tie with lexicographical comparison
                        if (CustomMinHeap.comparePath(existing.path, v, current.path, newHops) <= 0) {
                            strictlyDominated = true;
                            break;
                        }
                    }
                }

                if (strictlyDominated)
                    continue;

                CustomMinHeap.PathNode newPath = CustomMinHeap.getPathNode(v, current.path);
                CustomMinHeap.RouteNode newNode = CustomMinHeap.getRouteNode(v, newLatency, newHops, newPath);

                // Remove now-dominated routes from cache and invalidate them
                for (int i = v.bestRoutes.size() - 1; i >= 0; i--) {
                    CustomMinHeap.RouteNode existing = v.bestRoutes.get(i);
                    if (isDominated(existing, newNode, lambda)) {
                        existing.valid = false;
                        v.bestRoutes.remove(i);
                    }
                }

                v.bestRoutes.add(newNode);
                pq.push(newNode);
            }
        }

        return "No route found from " + id1 + " to " + id2;
    }

    // Helper to determine if one route dominates another
    private boolean isDominated(CustomMinHeap.RouteNode candidate, CustomMinHeap.RouteNode existing, int lambda) {
        if (lambda == 0) {
            // Standard domination
            int cmp = existing.compareTo(candidate);
            return cmp <= 0;
        } else {
            // Multi-objective domination
            if (existing.totalLatency <= candidate.totalLatency && existing.hops <= candidate.hops) {
                if (existing.totalLatency < candidate.totalLatency || existing.hops < candidate.hops) {
                    return true;
                }
                // Tie-breaker
                return CustomMinHeap.comparePath(existing.path, candidate.path, existing.hops + 1) <= 0;
            }
            return false;
        }
    }

    // Formats the output string for a successful trace
    private String formatRoute(CustomMinHeap.RouteNode node, String startId, String endId) {
        ArrayList<Host> path = node.path.toList();
        StringBuilder sb = new StringBuilder();
        sb.append("Optimal route ").append(startId).append(" -> ").append(endId).append(": ");
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i).id);
            if (i < path.size() - 1) {
                sb.append(" -> ");
            }
        }
        sb.append(" (Latency = ").append((int) node.totalLatency).append("ms)");
        return sb.toString();
    }

    // Checks connectivity of the whole network
    public String scanConnectivity() {
        if (hosts.size() <= 1) {
            return "Network is fully connected.";
        }

        int components = countComponents(null, null);
        if (components == 1) {
            return "Network is fully connected.";
        } else {
            return "Network has " + components + " disconnected components.";
        }
    }

    // Checks if removing a host increases component count (Articulation Point)
    public String simulateBreach(String id) {
        Host h = hostMap.get(id);
        if (h == null) {
            return "Some error occurred in simulate_breach.";
        }

        int originalComponents = countComponents(null, null);
        int newComponents = countComponents(h, null);

        if (newComponents > originalComponents) {
            return "Host " + id + " IS an articulation point.\nFailure results in " + newComponents
                    + " disconnected components.";
        } else {
            return "Host " + id + " is NOT an articulation point. Network remains the same.";
        }
    }

    // Checks if removing a backdoor increases component count (Bridge)
    public String simulateBreach(String id1, String id2) {
        Host h1 = hostMap.get(id1);
        Host h2 = hostMap.get(id2);
        if (h1 == null || h2 == null) {
            return "Some error occurred in simulate_breach.";
        }

        Backdoor target = null;
        for (Backdoor bd : h1.backdoors) {
            if (bd.getNeighbor(h1) == h2) {
                target = bd;
                break;
            }
        }

        if (target == null || target.isSealed) {
            return "Some error occurred in simulate_breach.";
        }

        int originalComponents = countComponents(null, null);
        int newComponents = countComponents(null, target);

        if (newComponents > originalComponents) {
            return "Backdoor " + id1 + " <-> " + id2 + " IS a bridge.\nFailure results in " + newComponents
                    + " disconnected components.";
        } else {
            return "Backdoor " + id1 + " <-> " + id2 + " is NOT a bridge. Network remains the same.";
        }
    }

    // Counts connected components using BFS, optionally ignoring a node/edge
    private int countComponents(Host ignoredHost, Backdoor ignoredBackdoor) {
        int count = 0;
        currentToken++; // Invalidate previous visit tokens

        for (Host h : hosts) {
            if (h == ignoredHost)
                continue;
            if (h.visitedToken != currentToken) {
                count++;
                bfs(h, ignoredHost, ignoredBackdoor);
            }
        }
        return count;
    }

    // BFS traversal
    private void bfs(Host start, Host ignoredHost, Backdoor ignoredBackdoor) {
        LinkedList<Host> q = new LinkedList<>();
        q.add(start);
        start.visitedToken = currentToken;

        while (!q.isEmpty()) {
            Host u = q.poll();
            for (Backdoor bd : u.backdoors) {
                if (bd == ignoredBackdoor)
                    continue;
                if (bd.isSealed)
                    continue;

                Host v = bd.getNeighbor(u);
                if (v == ignoredHost)
                    continue;

                if (v.visitedToken != currentToken) {
                    v.visitedToken = currentToken;
                    q.add(v);
                }
            }
        }
    }

    // Generates a summary report of network status
    public String oracleReport() {
        int totalHosts = hosts.size();
        int totalUnsealed = 0;
        double totalBandwidth = 0;
        double totalClearance = 0;

        for (Host h : hosts) {
            totalClearance += h.clearance;
            for (Backdoor bd : h.backdoors) {
                // Count each edge once (arbitrarily pick one side)
                if (h == bd.host1) {
                    if (!bd.isSealed) {
                        totalUnsealed++;
                        totalBandwidth += bd.bandwidth;
                    }
                }
            }
        }

        String connectivity = "Disconnected";
        int components = 0;
        if (totalHosts > 0) {
            components = countComponents(null, null);
            if (components == 1)
                connectivity = "Connected";
        } else {
            components = (totalHosts == 0) ? 0 : 1;
            connectivity = "Connected";
        }

        boolean cycles = hasCycles();

        BigDecimal avgBandwidth = BigDecimal.ZERO.setScale(1);
        if (totalUnsealed > 0) {
            avgBandwidth = BigDecimal.valueOf(totalBandwidth)
                    .divide(BigDecimal.valueOf(totalUnsealed), 1, RoundingMode.HALF_UP);
        }

        BigDecimal avgClearance = BigDecimal.ZERO.setScale(1);
        if (totalHosts > 0) {
            avgClearance = BigDecimal.valueOf(totalClearance)
                    .divide(BigDecimal.valueOf(totalHosts), 1, RoundingMode.HALF_UP);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("--- Resistance Network Report ---\n");
        sb.append("Total Hosts: ").append(totalHosts).append("\n");
        sb.append("Total Unsealed Backdoors: ").append(totalUnsealed).append("\n");
        sb.append("Network Connectivity: ").append(connectivity).append("\n");
        sb.append("Connected Components: ").append(components).append("\n");
        sb.append("Contains Cycles: ").append(cycles ? "Yes" : "No").append("\n");
        sb.append("Average Bandwidth: ").append(avgBandwidth).append("Mbps\n");
        sb.append("Average Clearance Level: ").append(avgClearance);

        return sb.toString();
    }

    // Checks for cycles in the graph
    private boolean hasCycles() {
        currentToken++;

        for (Host h : hosts) {
            if (h.visitedToken != currentToken) {
                if (dfsCycle(h, null))
                    return true;
            }
        }
        return false;
    }

    // DFS for cycle detection
    private boolean dfsCycle(Host u, Host parent) {
        u.visitedToken = currentToken;
        for (Backdoor bd : u.backdoors) {
            if (bd.isSealed)
                continue;
            Host v = bd.getNeighbor(u);

            if (v == parent)
                continue;

            if (v.visitedToken == currentToken)
                return true;

            if (dfsCycle(v, u))
                return true;
        }
        return false;
    }
}
