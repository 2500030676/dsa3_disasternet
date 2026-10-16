package edu.disasternet.algorithms.np;

import java.util.*;

/**
 * Small exact SAT/clique/cover solvers and a 2-approximation for response
 * constraints and critical-link coverage. Brute-force SAT/clique/optimal cover
 * take exponential time; the matching-based 2-approx vertex cover takes O(E)
 * on adjacency lists (this teaching implementation receives an adjacency matrix).
 * Keep exact demonstrations small; use the approximation for larger instances.
 */
public final class NpAlgorithms {

    private NpAlgorithms() {}

    public record Literal(String variable, boolean positive) {}
    public record SatResult(boolean satisfiable, Map<String, Boolean> assignment) {}

    public static SatResult sat(List<List<Literal>> clauses) {
        Set<String> vars = new LinkedHashSet<>();
        clauses.forEach(c -> c.forEach(l -> vars.add(l.variable)));
        List<String> v = new ArrayList<>(vars);

        for (int mask = 0; mask < (1 << v.size()); mask++) {
            Map<String, Boolean> a = new LinkedHashMap<>();
            for (int i = 0; i < v.size(); i++) {
                a.put(v.get(i), (mask & (1 << i)) != 0);
            }

            boolean ok = true;
            for (List<Literal> c : clauses) {
                boolean any = false;
                for (Literal l : c) {
                    if (a.get(l.variable) == l.positive) any = true;
                }
                ok &= any;
            }
            if (ok) return new SatResult(true, a);
        }
        return new SatResult(false, Map.of());
    }

    public static Set<Integer> maximumClique(boolean[][] g) {
        Set<Integer> best = new LinkedHashSet<>();
        for (int m = 0; m < (1 << g.length); m++) {
            Set<Integer> x = new LinkedHashSet<>();
            for (int i = 0; i < g.length; i++) if ((m & (1 << i)) > 0) x.add(i);

            boolean ok = true;
            for (int i : x) for (int j : x) if (i != j && !g[i][j]) ok = false;

            if (ok && x.size() > best.size()) best = x;
        }
        return best;
    }

    public static Set<Integer> independentSet(boolean[][] g) {
        boolean[][] c = complement(g);
        return maximumClique(c);
    }

    public static boolean[][] complement(boolean[][] g) {
        int n = g.length;
        boolean[][] c = new boolean[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                c[i][j] = i != j && !g[i][j];
        return c;
    }

    public static Set<Integer> vertexCover(boolean[][] g) {
        int n = g.length;
        for (int m = 0; m < (1 << n); m++) {
            Set<Integer> x = new LinkedHashSet<>();
            for (int i = 0; i < n; i++) if ((m & (1 << i)) > 0) x.add(i);

            boolean ok = true;
            for (int i = 0; i < n; i++)
                for (int j = i + 1; j < n; j++)
                    if (g[i][j] && !x.contains(i) && !x.contains(j)) ok = false;

            if (ok) return x;
        }
        return Set.of();
    }

    public static Set<Integer> vertexCover2Approx(boolean[][] g) {
        int n = g.length;
        boolean[][] r = new boolean[n][n];
        for (int i = 0; i < n; i++) r[i] = g[i].clone();

        Set<Integer> c = new LinkedHashSet<>();
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)
                if (r[i][j]) {
                    c.add(i);
                    c.add(j);
                    for (int k = 0; k < n; k++) {
                        r[i][k] = r[k][i] = r[j][k] = r[k][j] = false;
                    }
                }
        return c;
    }

    /** Builds 3-SAT compatibility graph: nodes are literals; edges join literals in distinct clauses that are not contradictory. */
    public static boolean[][] threeSatToClique(List<List<Literal>> clauses) {
        List<Literal> nodes = new ArrayList<>();
        clauses.forEach(nodes::addAll);

        boolean[][] g = new boolean[nodes.size()][nodes.size()];
        int[] clause = new int[nodes.size()];
        int z = 0;

        for (int i = 0; i < clauses.size(); i++)
            for (int j = 0; j < clauses.get(i).size(); j++)
                clause[z++] = i;

        for (int i = 0; i < z; i++)
            for (int j = i + 1; j < z; j++)
                g[i][j] = g[j][i] =
                    clause[i] != clause[j] &&
                    (!nodes.get(i).variable.equals(nodes.get(j).variable)
                        || nodes.get(i).positive == nodes.get(j).positive);

        return g;
    }
}
