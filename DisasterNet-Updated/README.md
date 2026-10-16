# DisasterNet – Emergency Response Coordination Platform

## High-Level Description

DisasterNet models an emergency-response platform that manages:

- emergency requests and incident reports
- rescue teams and volunteers
- shelters and relief centers
- medical supplies and essential resources
- emergency communication records
- continuous field observations

The system processes large-scale emergency data and supports rapid information retrieval, document indexing, data comparison, resource distribution, conflict handling, and real-time emergency analytics.

This project is a DSA-driven academic system, not a CRUD or full-stack management application. Every module demonstrates a meaningful algorithm applied to a real emergency-response problem.

## Architecture

`requests/reports/observations -> CO1 retrieval -> CO2 document index -> CO3 correction/resource selection -> CO4 supply distribution/team assignment -> CO5 constraints/conflicts -> CO6 real-time analytics -> response summary`

Source is deliberately separated by concern:

- `src/main/java/edu/disasternet/algorithms/` — hand-written reusable algorithms.
- `src/main/java/edu/disasternet/simulations/` — DisasterNet scenario orchestration.
- `src/main/java/edu/disasternet/cli/` — interactive terminal menus, including the algorithm laboratory and sample-data statistics.
- `src/main/java/edu/disasternet/data/DataStore.java` — shared in-memory records used by every menu for the whole run.
- `src/main/java/edu/disasternet/models/` — incidents, teams, volunteers, shelters, supplies, messages and observations.
- `src/test/java/` — correctness assertions.
- `data/` — realistic flood, medical, blockage, fire, shortage and field-observation samples.

## Where Each DSA Fits

### String Algorithms (M1 – CO1)
- KMP for searching emergency keywords and incident codes.
- Z-Function for detecting repeated emergency patterns.
- Rabin-Karp for searching supply and incident identifiers.
- Aho-Corasick for detecting multiple emergency keywords simultaneously.

### Suffix Structures (M2 – CO2)
- Suffix Arrays for indexing emergency reports.
- SA-IS for large-scale incident-document indexing.
- LCP/Kasai for identifying repeated incident descriptions.
- Suffix Automaton for rapid substring retrieval.

### Advanced Dynamic Programming (M3 – CO3)
- Levenshtein/Damerau-Levenshtein for correcting noisy emergency reports.
- Weighted Edit Distance for prioritizing different reporting errors.
- Bitmask DP for selecting combinations of emergency resources.
- Matrix-Chain Multiplication for optimizing emergency-data processing.

### Network Flow (M4 – CO4)
- Ford-Fulkerson for emergency-supply distribution.
- Edmonds-Karp for relief-flow networks.
- Dinic's algorithm for large emergency networks.
- Bipartite Matching for assigning rescue teams to relief activities.
- König's theorem for analyzing team-activity conflicts.
- Max-Flow Min-Cut for identifying critical supply bottlenecks.

### NP-Completeness and Approximation (M5 – CO5)
- SAT/3-SAT for emergency-resource constraints.
- 3-SAT → CLIQUE for demonstrating constraint transformation.
- CLIQUE → INDEPENDENT-SET → VERTEX-COVER for emergency-conflict analysis.
- Vertex-Cover 2-approximation for selecting locations covering critical connections.

### Randomized and Parallel Algorithms (M6 – CO6)
- Randomized QuickSort for ranking emergency requests.
- Reservoir Sampling for continuously arriving field observations.
- Miller-Rabin for large numerical computations.
- Blelloch Scan for cumulative emergency statistics.
- Parallel Reduce for aggregating field observations.
- Brent's theorem for evaluating parallel emergency-data processing.

## CO mapping and DisasterNet purpose

| CO | Topic | Implementations | DisasterNet feature |
|---|---|---|---|
| CO1 | String Algorithms | KMP, Z, Rabin–Karp, Aho–Corasick | keyword, ID and multi-alert detection |
| CO2 | Suffix Structures | suffix array, SA-IS, Kasai LCP, suffix automaton | document index and repeated phrase analysis |
| CO3 | Advanced DP | Levenshtein, Damerau, weighted edit, bitmask DP, matrix chain | typo repair, package selection, analytics pipeline plan |
| CO4 | Network Flow | Ford-Fulkerson, Edmonds-Karp, Dinic, matching, Konig, min cut | warehouse, relief-centre and shelter routing; bottlenecks; team-activity assignment |
| CO5 | NP Completeness | SAT, 3-SAT→Clique, clique, independent set, vertex cover, 2-approx | resource constraints and communication coverage |
| CO6 | Randomized + Parallel | randomized quicksort, reservoir sampling, Miller–Rabin, Blelloch scan, reduce, Brent | priorities, field stream and parallel analytics |

## Complexity dashboard

| Algorithm | Time | Extra space |
|---|---:|---:|
| KMP / Z | O(n+m) | O(n+m) for Z; O(m) for KMP |
| Rabin–Karp | expected O(n+m), worst O(nm) | O(1) |
| Aho–Corasick | O(text + matches + dictionary) | O(total pattern length) |
| suffix array (prefix doubling) | O(n log² n) | O(n) |
| SA-IS / Kasai / suffix automaton | O(n) | O(n) |
| edit distances | O(mn) | O(mn) |
| 0/1 bitmask resource DP | O(nB) | O(B) |
| matrix-chain DP | O(n³) | O(n²) |
| Ford–Fulkerson | O(E·max-flow) integer capacities | O(V+E) |
| Edmonds–Karp | O(VE²) | O(V+E) |
| Dinic | O(V²E), general capacities | O(V+E) |
| bipartite matching / König | O(VE) | O(V+E) |
| brute-force SAT / clique / cover | exponential | exponential / O(V) |
| 2-approx vertex cover (matrix input) | O(V²) | O(V) |
| randomized quicksort | expected O(n log n), worst O(n²) | expected O(log n) stack |
| reservoir sample / Miller–Rabin | O(n) / O(log² n) for fixed bases | O(k) / O(1) |
| Blelloch scan / reduction | O(n) work, O(log n) span | O(n) |

The vertex-cover routine is explicitly an approximation: it returns at most twice the optimum cover size, not necessarily the optimum.

## Run

Prerequisite: Java 17+ (JDK with `javac` and `java`). Maven is optional.

```powershell
.\build.ps1
java -cp target/classes edu.disasternet.Main
```

To run the integrated non-interactive demo, compile with `.\build.ps1` and then run `java -cp target/classes edu.disasternet.Main demo`.

In VS Code, open this project folder and select **DisasterNet: Terminal Control Center** from **Run and Debug**. The included pre-launch task compiles `src/main/java` into `target/classes` before starting the interactive terminal app. Select **DisasterNet: Integrated DSA Demo** to run the summary directly. A JDK 17+ must be available on `PATH` as `javac`.

The `demo` prints a measured end-to-end emergency summary. The interactive menu's Algorithm Laboratory runs the same algorithms against the shipped report data.

### Enter a disaster scenario

From the terminal control center, select **7. Algorithm Laboratory** and then **1** to run the integrated emergency-response scenario against the sample report stream. The scenario connects string search, suffix indexing, typo correction, resource selection, supply flow, team assignment, observation sampling and cumulative analytics.

The laboratory also offers focused demonstrations: **2** uses DP against the stocked resource types and includes Damerau-Levenshtein and matrix-chain planning; **3** solves emergency constraints with SAT and displays a 3-SAT-to-clique/vertex-cover approximation; **4** runs randomized sorting, reservoir sampling, Miller-Rabin, Blelloch scan, parallel reduction and Brent's estimate on current incident/observation records. The module menus connect KMP/Aho-Corasick/Z/Rabin-Karp, Dinic/min-cut, resource knapsack, bipartite team assignment and observation analytics to their working records.

The app seeds 20 incidents, 10 teams, 20 volunteers, 10 shelters, 20 supply types, 30 communication records and 50 observations. All menus share those mutable records through `DataStore`; additions, allocations and occupancy changes are reflected across modules and statistics for the current run. This is in-memory session state: closing the program resets it. The JSON/text files under `data/` are separate example/scale datasets and are not automatically rewritten by menu actions.

## Dataset tiers and scaling

`data/` ships a small, human-readable scenario. `DatasetGenerator` creates reproducible linked scale datasets containing reports, incidents, observations, teams, shelters, resources, and communication records:

```powershell
.\build.ps1
java -cp target/classes edu.disasternet.data.DatasetGenerator
```

It writes `data/generated/reports-100`, `data/generated/reports-1000`, and `data/generated/reports-10000`. Reports cover floods, cyclones, earthquakes, fires, landslides, medical emergencies, road blockages, shelter shortages, evacuation and missing-person requests. Measured timing must always be collected from the actual run rather than claimed in documentation.

## Why these algorithms were selected

String matching detects urgent language efficiently; suffix indexes support repeated investigative queries; edit DP corrects radio/field typos; knapsack maximizes coverage under budget; max flow exposes capacity-constrained supply movement; matching assigns constrained teams; SAT expresses activate-if-available rules; graph reductions demonstrate NP relationships; and randomized/parallel methods address large continuous observation streams.

## Limitations and future work

This is a deterministic teaching simulator, not a live dispatch system. The SAT/clique/optimal-cover solvers are intentionally exhaustive and suitable only for small demonstration instances. Future work: authenticated data ingestion, geographic rendering, persisted scenario files, and controlled multi-threaded benchmarking.

## Viva questions with concise answers

1. **Why KMP for report search?** Its prefix table prevents rechecking matched characters and guarantees O(n+m).
2. **Why Aho–Corasick instead of repeated KMP?** One trie/failure automaton searches all emergency words in one text scan.
3. **How does min cut help dispatch?** Cut edges identify capacity bottlenecks separating warehouses from affected areas.
4. **What does König prove here?** In a bipartite team-task graph, maximum assignments equal the minimum number of team/task vertices that cover all compatibility edges.
5. **Is Miller–Rabin always probabilistic?** Generally yes; the fixed bases used here are deterministic for signed 64-bit integers, while the principle is probabilistic for arbitrary-size integers.
6. **Does 2-approx vertex cover find the best stations?** No; it guarantees a cover no larger than twice the optimum.


