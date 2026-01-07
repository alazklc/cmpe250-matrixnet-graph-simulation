// Represents a connection between two hosts
public class Backdoor {
    public Host host1; // First endpoint
    public Host host2; // Second endpoint
    public int latency; // Latency in ms
    public int bandwidth; // Bandwidth in Mbps
    public int firewall; // Minimum clearance required
    public boolean isSealed; // Status of the link

    // Initializes a new backdoor link
    public Backdoor(Host host1, Host host2, int latency, int bandwidth, int firewall) {
        this.host1 = host1;
        this.host2 = host2;
        this.latency = latency;
        this.bandwidth = bandwidth;
        this.firewall = firewall;
        this.isSealed = false;
    }

    // Returns the connected neighbor of the given host
    public Host getNeighbor(Host h) {
        if (h == host1)
            return host2;
        if (h == host2)
            return host1;
        return null;
    }
}
