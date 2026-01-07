# MatrixNet: Resistance Network Simulation 🟢💻

> **CMPE250 - Data Structures and Algorithms Project**
>
> *A graph-based network simulation engine inspired by "The Matrix". It optimizes covert routing, detects critical infrastructure vulnerabilities, and manages secure tunnels using custom-built algorithms.*

## 📖 About the Project
**MatrixNet** is a simulation tool designed for the "Resistance" to establish and secure a clandestine network against "Agents". The system models hosts as graph nodes and backdoors as edges with specific properties (bandwidth, latency, firewall level).

The core challenge was to implement complex **Graph Algorithms** without using standard Java libraries (no `PriorityQueue` or `HashMap`), requiring high-performance custom implementations to handle massive datasets under strict time constraints.

## 🚀 Technical Highlights & Algorithms
This project implements advanced graph theory concepts and custom data structures:

* **Multi-Objective Shortest Path (Modified Dijkstra):**
    * Implemented a **Custom Min-Heap** optimized for pathfinding.
    * Routing logic prioritizes **Latency > Hops > Lexicographical Order** simultaneously.
    * Supports a dynamic congestion factor ($\lambda$) where edge costs scale based on path length.
* **Network Vulnerability Analysis:**
    * **Articulation Points & Bridges:** Simulates node/edge failures to detect critical weak points that would disconnect the network components.
    * **Cycle Detection:** Uses DFS (Depth-First Search) to identify routing loops.
    * **Connectivity Scans:** Uses BFS (Breadth-First Search) to count connected components.
* **Custom Data Structures:**
    * `CustomHashMap`: A bucket-based hash map with chaining for $O(1)$ host lookups.
    * `CustomMinHeap`: A binary heap tailored for route objects, supporting zero-allocation path comparisons.

## 📂 Project Structure

```text
matrixnet-simulation/
├── src/
│   ├── Main.java           # Entry point & Command Parser
│   ├── MatrixNet.java      # Graph Manager & Algorithms (BFS, DFS, Breach Sim)
│   ├── CustomMinHeap.java  # Optimized Heap for Dijkstra's Algorithm
│   ├── CustomHashMap.java  # Hash Map implementation from scratch
│   ├── Host.java           # Graph Node (Vertex)
│   └── Backdoor.java       # Graph Edge with weights (Latency, Bandwidth)
