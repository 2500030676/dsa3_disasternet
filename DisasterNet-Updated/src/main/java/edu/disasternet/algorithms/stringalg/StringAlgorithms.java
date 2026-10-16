package edu.disasternet.algorithms.stringalg;

import java.util.*;

/**
 * Manual string algorithms used by emergency reports, message IDs and alert phrases.
 * KMP: O(n+m) time/O(m) space; Z: O(n+m) time/O(n+m) space;
 * Rabin-Karp: expected O(n+m), worst O(nm) time/O(1) space;
 * Aho-Corasick: O(text + matches + total pattern length) time and
 * O(total pattern length) automaton space. Lowercase input at feature boundaries
 * when case-insensitive matching is desired.
 */
public final class StringAlgorithms {

    private StringAlgorithms() {}

    public record SearchTrace(
        List<Integer> matches,
        int comparisons,
        int hashComparisons,
        int falseMatches,
        int[] auxiliary
    ) {}

    public static SearchTrace kmp(String text, String pattern) {
        if (pattern.isEmpty())
            return new SearchTrace(IntStreamRange(text.length() + 1), 0, 0, 0, new int[0]);

        int[] lps = new int[pattern.length()];
        for (int i = 1, len = 0; i < pattern.length();) {
            if (pattern.charAt(i) == pattern.charAt(len)) lps[i++] = ++len;
            else if (len > 0) len = lps[len - 1];
            else i++;
        }

        List<Integer> out = new ArrayList<>();
        int cmp = 0;
        for (int i = 0, j = 0; i < text.length();) {
            cmp++;
            if (text.charAt(i) == pattern.charAt(j)) {
                i++; j++;
                if (j == pattern.length()) {
                    out.add(i - j);
                    j = lps[j - 1];
                }
            } else if (j > 0) j = lps[j - 1];
            else i++;
        }
        return new SearchTrace(out, cmp, 0, 0, lps);
    }

    public static SearchTrace zSearch(String text, String pattern) {
        String s = pattern + "$" + text;
        int n = s.length(), l = 0, r = 0;
        int[] z = new int[n];
        List<Integer> o = new ArrayList<>();

        for (int i = 1; i < n; i++) {
            if (i <= r) z[i] = Math.min(r - i + 1, z[i - l]);
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) z[i]++;
            if (i + z[i] - 1 > r) { l = i; r = i + z[i] - 1; }
            if (z[i] >= pattern.length()) o.add(i - pattern.length() - 1);
        }
        return new SearchTrace(o, 0, 0, 0, z);
    }

    public static SearchTrace rabinKarp(String text, String p) {
        if (p.isEmpty())
            return new SearchTrace(IntStreamRange(text.length() + 1), 0, 0, 0, new int[0]);

        long base = 257, mod = 1_000_000_007L, ph = 0, th = 0, pow = 1;
        for (int i = 0; i < p.length(); i++) {
            ph = (ph * base + p.charAt(i)) % mod;
            if (i < text.length()) th = (th * base + text.charAt(i)) % mod;
            if (i < p.length() - 1) pow = pow * base % mod;
        }

        List<Integer> o = new ArrayList<>();
        int hc = 0, fm = 0, cc = 0;
        for (int i = 0; i + p.length() <= text.length(); i++) {
            if (ph == th) {
                hc++;
                boolean same = true;
                for (int j = 0; j < p.length(); j++) {
                    cc++;
                    if (text.charAt(i + j) != p.charAt(j)) { same = false; break; }
                }
                if (same) o.add(i);
                else fm++;
            }
            if (i + p.length() < text.length())
                th = ((th - text.charAt(i) * pow % mod + mod) * base + text.charAt(i + p.length())) % mod;
        }
        return new SearchTrace(o, cc, hc, fm, new int[0]);
    }

    public static List<Integer> IntStreamRange(int n) {
        List<Integer> x = new ArrayList<>();
        for (int i = 0; i < n; i++) x.add(i);
        return x;
    }

    public static final class AhoCorasick {
        static class Node {
            Map<Character, Integer> next = new HashMap<>();
            int fail;
            List<String> out = new ArrayList<>();
        }

        final List<Node> nodes = new ArrayList<>(List.of(new Node()));

        public AhoCorasick(Collection<String> words) {
            for (String w : words) add(w.toUpperCase());
            Queue<Integer> q = new ArrayDeque<>();
            for (int c : nodes.get(0).next.values()) q.add(c);

            while (!q.isEmpty()) {
                int v = q.remove();
                for (var e : nodes.get(v).next.entrySet()) {
                    q.add(e.getValue());
                    int f = nodes.get(v).fail;
                    while (f != 0 && !nodes.get(f).next.containsKey(e.getKey())) f = nodes.get(f).fail;
                    nodes.get(e.getValue()).fail = nodes.get(f).next.getOrDefault(e.getKey(), 0);
                    nodes.get(e.getValue()).out.addAll(nodes.get(nodes.get(e.getValue()).fail).out);
                }
            }
        }

        void add(String w) {
            int v = 0;
            for (char c : w.toCharArray()) {
                Integer nx = nodes.get(v).next.get(c);
                if (nx == null) {
                    nx = nodes.size();
                    nodes.get(v).next.put(c, nx);
                    nodes.add(new Node());
                }
                v = nx;
            }
            nodes.get(v).out.add(w);
        }

        public Map<String, List<Integer>> search(String text) {
            Map<String, List<Integer>> r = new LinkedHashMap<>();
            int v = 0;
            char[] a = text.toUpperCase().toCharArray();
            for (int i = 0; i < a.length; i++) {
                while (v != 0 && !nodes.get(v).next.containsKey(a[i])) v = nodes.get(v).fail;
                v = nodes.get(v).next.getOrDefault(a[i], 0);
                for (String w : nodes.get(v).out)
                    r.computeIfAbsent(w, k -> new ArrayList<>()).add(i - w.length() + 1);
            }
            return r;
        }

        public int states() { return nodes.size(); }
    }
}
