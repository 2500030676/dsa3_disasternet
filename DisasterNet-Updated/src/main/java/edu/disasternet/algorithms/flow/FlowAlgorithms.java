
package edu.disasternet.algorithms.flow;

import java.util.*;

/**
 * Hand-built residual graph algorithms for moving relief capacity and assigning
 * compatible teams. Ford-Fulkerson is O(E·F) for integral flow F;
 * Edmonds-Karp is O(VE^2); Dinic is O(V^2E) for general capacities.
 * Bipartite augmenting-path matching is O(VE). Graph storage is O(V+E).
 */
public final class FlowAlgorithms {

    private FlowAlgorithms() {}

    public record Edge(int from, int to, int capacity) {}
    public record FlowResult(int maxFlow, List<String> paths, Set<Integer> sourceSide) {}

    static class R {
        int to, rev, cap, original;
        R(int t, int r, int c) { to = t; rev = r; cap = original = c; }
    }

    static List<R>[] graph(int n, List<Edge> es) {
        List<R>[] g = new List[n];
        for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
        for (Edge e : es) {
            R a = new R(e.to, g[e.to].size(), e.capacity);
            R b = new R(e.from, g[e.from].size(), 0);
            g[e.from].add(a);
            g[e.to].add(b);
        }
        return g;
    }

    public static FlowResult fordFulkerson(int n, List<Edge> e, int s, int t) {
        List<R>[] g = graph(n, e);
        List<String> tr = new ArrayList<>();
        int f = 0;
        while (true) {
            int[] par = new int[n], pei = new int[n];
            Arrays.fill(par, -1);
            if (!dfsPath(g, s, t, par, pei, new boolean[n])) break;
            int add = bottleneck(g, s, t, par, pei);
            augment(g, s, t, par, pei, add);
            f += add;
            tr.add(path(par, t) + " bottleneck=" + add);
        }
        return new FlowResult(f, tr, reachable(g, s));
    }

    static boolean dfsPath(List<R>[] g, int v, int t, int[] p, int[] pi, boolean[] seen) {
        if (v == t) return true;
        seen[v] = true;
        for (int i = 0; i < g[v].size(); i++) {
            R x = g[v].get(i);
            if (x.cap > 0 && !seen[x.to]) {
                p[x.to] = v;
                pi[x.to] = i;
                if (dfsPath(g, x.to, t, p, pi, seen)) return true;
            }
        }
        return false;
    }

    public static FlowResult edmondsKarp(int n, List<Edge> e, int s, int t) {
        List<R>[] g = graph(n, e);
        List<String> tr = new ArrayList<>();
        int f = 0;
        while (true) {
            int[] p = new int[n], pi = new int[n];
            Arrays.fill(p, -1);
            Queue<Integer> q = new ArrayDeque<>();
            q.add(s);
            p[s] = s;
            while (!q.isEmpty() && p[t] < 0) {
                int u = q.remove();
                for (int i = 0; i < g[u].size(); i++) {
                    R x = g[u].get(i);
                    if (x.cap > 0 && p[x.to] < 0) {
                        p[x.to] = u;
                        pi[x.to] = i;
                        q.add(x.to);
                    }
                }
            }
            if (p[t] < 0) break;
            int a = bottleneck(g, s, t, p, pi);
            augment(g, s, t, p, pi, a);
            f += a;
            tr.add(path(p, t) + " bottleneck=" + a);
        }
        return new FlowResult(f, tr, reachable(g, s));
    }

    public static FlowResult dinic(int n, List<Edge> e, int s, int t) {
        List<R>[] g = graph(n, e);
        int total = 0;
        List<String> tr = new ArrayList<>();
        int[] level = new int[n];
        while (level(g, s, t, level)) {
            int[] it = new int[n];
            int a;
            while ((a = send(g, s, t, Integer.MAX_VALUE, level, it)) > 0) {
                total += a;
                tr.add("level graph blocking augmentation=" + a);
            }
        }
        return new FlowResult(total, tr, reachable(g, s));
    }

    static boolean level(List<R>[] g, int s, int t, int[] l) {
        Arrays.fill(l, -1);
        Queue<Integer> q = new ArrayDeque<>();
        q.add(s);
        l[s] = 0;
        while (!q.isEmpty()) {
            int u = q.remove();
            for (R e : g[u]) if (e.cap > 0 && l[e.to] < 0) {
                l[e.to] = l[u] + 1;
                q.add(e.to);
            }
        }
        return l[t] >= 0;
    }

    static int send(List<R>[] g, int v, int t, int f, int[] l, int[] it) {
        if (v == t) return f;
        for (; it[v] < g[v].size(); it[v]++) {
            R e = g[v].get(it[v]);
            if (e.cap > 0 && l[e.to] == l[v] + 1) {
                int got = send(g, e.to, t, Math.min(f, e.cap), l, it);
                if (got > 0) {
                    e.cap -= got;
                    g[e.to].get(e.rev).cap += got;
                    return got;
                }
            }
        }
        return 0;
    }

    static int bottleneck(List<R>[] g, int s, int t, int[] p, int[] pi) {
        int a = Integer.MAX_VALUE;
        for (int v = t; v != s; v = p[v]) {
            a = Math.min(a, g[p[v]].get(pi[v]).cap);
        }
        return a;
    }

    static void augment(List<R>[] g, int s, int t, int[] p, int[] pi, int a) {
        for (int v = t; v != s; v = p[v]) {
            R x = g[p[v]].get(pi[v]);
            x.cap -= a;
            g[v].get(x.rev).cap += a;
        }
    }

    static String path(int[] p, int t) {
        List<Integer> x = new ArrayList<>();
        for (int v = t; v >= 0; v = p[v]) {
            x.add(v);
            if (p[v] == v) break;
        }
        Collections.reverse(x);
        return x.toString();
    }

    static Set<Integer> reachable(List<R>[] g, int s) {
        Set<Integer> r = new LinkedHashSet<>();
        Queue<Integer> q = new ArrayDeque<>();
        q.add(s);
        r.add(s);
        while (!q.isEmpty()) {
            for (R e : g[q.remove()]) {
                if (e.cap > 0 && r.add(e.to)) q.add(e.to);
            }
        }
        return r;
    }

    public static Map<String, String> bipartiteMatching(Map<String, List<String>> adj) {
        Map<String, String> task = new LinkedHashMap<>();
        for (String team : adj.keySet()) {
            augment(team, adj, task, new HashSet<>());
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (var x : task.entrySet()) result.put(x.getValue(), x.getKey());
        return result;
    }

    static boolean augment(String team, Map<String, List<String>> a,
                           Map<String, String> m, Set<String> seen) {
        for (String task : a.getOrDefault(team, List.of())) {
            if (seen.add(task) && (!m.containsKey(task) || augment(m.get(task), a, m, seen))) {
                m.put(task, team);
                return true;
            }
        }
        return false;
    }

    public static Set<String> konigCover(Map<String, List<String>> a, Map<String, String> match) {
        Set<String> left = new HashSet<>(a.keySet());
        Set<String> matchedL = new HashSet<>(match.values());
        Set<String> zl = new HashSet<>(), zr = new HashSet<>();
        Queue<String> q = new ArrayDeque<>();
        for (String x : left) if (!matchedL.contains(x)) {
            zl.add(x);
            q.add(x);
        }
        while (!q.isEmpty()) {
            String u = q.remove();
            for (String v : a.getOrDefault(u, List.of())) {
                if (!match.containsKey(v) || !match.get(v).equals(u)) {
                    if (zr.add(v) && match.containsKey(v) && zl.add(match.get(v))) {
                        q.add(match.get(v));
                    }
                }
            }
        }
        Set<String> cover = new LinkedHashSet<>();
        for (String u : left) if (!zl.contains(u)) cover.add("Team:" + u);
        for (String v : zr) cover.add("Task:" + v);
        return cover;
    }
}
