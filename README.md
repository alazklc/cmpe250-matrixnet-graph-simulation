# MatrixNet: Resistance Network Simulation

**Course:** CMPE250 - Data Structures and Algorithms  
**Context:** Graph Theory & Network Optimization Project

## Project Overview

MatrixNet is a high-performance graph simulation engine designed to optimize covert routing protocols and analyze network topology under strict constraints. Inspired by the concept of a "Resistance" network, the system models hosts as nodes and secure backdoors as weighted edges, focusing on secure tunnel management and vulnerability detection.

This project demonstrates advanced understanding of Graph Theory and low-level data structure implementation. A key constraint of this development was the strictly prohibited use of standard Java Collection libraries (e.g., `PriorityQueue`, `HashMap`), necessitating the creation of custom, highly optimized alternatives.

## Core Capabilities

* **Precision Routing:** Calculates optimal paths based on multi-layered criteria, prioritizing low latency and minimal hop counts while navigating firewall restrictions.
* **Vulnerability Detection:** Identifies critical points of failure (Articulation Points and Bridges) within the network infrastructure.
* **Infrastructure Analysis:** Detects routing loops and isolates independent network segments to ensure structural integrity.

## Technical Implementation

The engine relies on custom algorithms and data structures to handle large datasets within strict time and memory limits.

### 1. Multi-Objective Pathfinding (Modified Dijkstra)
Standard shortest-path algorithms were adapted to handle a hierarchy of optimization criteria. The routing engine minimizes costs based on the following priority:
1.  **Latency:** The primary weight of the edge.
2.  **Hop Count:** The number of intermediate nodes.
3.  **Lexicographical Order:** Tie-breaking mechanism for node identifiers.

The algorithm also supports a dynamic congestion factor ($\lambda$), where edge costs scale relative to the path length, simulating active network load.

### 2. Network Vulnerability Analysis
MatrixNet implements critical graph traversal algorithms to assess network robustness:
* **Articulation Points & Bridges:** Simulates node and edge failures to identify components that, if removed, would sever network connectivity.
* **Cycle Detection:** Utilizes Depth-First Search (DFS) to identify and report routing loops.
* **Connectivity Scans:** Utilizes Breadth-First Search (BFS) to enumerate and analyze connected components.

### 3. Custom Data Structures
To meet performance requirements without standard libraries, the following structures were implemented from scratch:

* **`CustomHashMap`:** A bucket-based hash map implementation using chaining for collision resolution. Optimized for $O(1)$ average-case host lookups.
* **`CustomMinHeap`:** A binary heap specifically tailored for route objects. It supports efficient `decrease-key` operations and zero-allocation path comparisons to accelerate the Dijkstra implementation.

## Project Structure

```text
matrixnet-simulation/
├── src/
│   ├── Main.java           # Entry point, command parser, and I/O handling
│   ├── MatrixNet.java      # Core graph manager and algorithm implementations
│   ├── CustomMinHeap.java  # Optimized binary heap for priority queue operations
│   ├── CustomHashMap.java  # Native hash map implementation with chaining
│   ├── Host.java           # Vertex representation (Network Host)
│   └── Backdoor.java       # Edge representation with weights (Latency, Bandwidth)
