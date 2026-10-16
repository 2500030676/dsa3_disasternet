package edu.disasternet.algorithms.suffix;

import java.util.*;

/**
 * Text indexes for matching repeated phrases in combined emergency reports.
 * Prefix-doubling suffix array: O(n log^2 n) time/O(n) space;
 * SA-IS and Kasai LCP: O(n) time/O(n) space; suffix automaton: O(n)
 * expected construction time and O(n) states. Strings use Java UTF-16 code units.
 */
public final class SuffixStructures {

    private SuffixStructures() {}

    public static int[] suffixArray(String s) {
        int n = s.length();
        Integer[] order = new Integer[n];
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) { order[i] = i; rank[i] = s.charAt(i); }
        for (int width = 1; width < n; width = width > n / 2 ? n : width * 2) {
            final int step = width;
            final int[] currentRank = rank;
            Arrays.sort(order, Comparator.comparingInt((Integer i) -> currentRank[i])
                .thenComparingInt(i -> i + step < n ? currentRank[i + step] : -1));
            int[] next = new int[n];
            for (int i = 1; i < n; i++) {
                int a = order[i - 1], b = order[i];
                boolean differs = rank[a] != rank[b]
                    || (a + step < n ? rank[a + step] : -1) != (b + step < n ? rank[b + step] : -1);
                next[b] = next[a] + (differs ? 1 : 0);
            }
            rank = next;
            if (n == 0 || rank[order[n - 1]] == n - 1) break;
        }
        return Arrays.stream(order).mapToInt(Integer::intValue).toArray();
    }

    /** Educational SA-IS: coordinate-compresses chars and induced-sorts LMS substrings recursively. */
    public static int[] sais(String s) {
        int[] x = s.chars().toArray();
        if (x.length == 0) return new int[0];
        int[] a = new int[x.length + 1];
        for (int i = 0; i < x.length; i++) a[i] = x[i] + 1;
        int maxSymbol = Arrays.stream(a).max().orElse(0);
        int[] withSentinel = saisInt(a, maxSymbol + 1);
        return Arrays.stream(withSentinel).filter(i -> i < x.length).toArray();
    }

    private static int[] saisInt(int[] s, int k) {
        int n = s.length;
        if (n == 1) return new int[]{0};

        boolean[] t = new boolean[n];
        t[n - 1] = true;
        for (int i = n - 2; i >= 0; i--)
            t[i] = s[i] < s[i + 1] || (s[i] == s[i + 1] && t[i + 1]);

        List<Integer> lms = new ArrayList<>();
        for (int i = 1; i < n; i++) if (t[i] && !t[i - 1]) lms.add(i);

        int[] sa = induce(s, k, t, lms);

        List<Integer> sorted = new ArrayList<>();
        for (int v : sa) if (v > 0 && t[v] && !t[v - 1]) sorted.add(v);

        int[] name = new int[n];
        int names = 0, prev = -1;
        for (int p : sorted) {
            if (prev < 0 || !same(s, t, prev, p)) names++;
            name[p] = names - 1;
            prev = p;
        }

        List<Integer> order;
        if (names == lms.size()) {
            order = new ArrayList<>(lms);
            order.sort(Comparator.comparingInt(i -> name[i]));
        } else {
            int[] rec = new int[lms.size()];
            for (int i = 0; i < lms.size(); i++) rec[i] = name[lms.get(i)];
            int[] rsa = saisInt(rec, names);
            order = new ArrayList<>();
            for (int v : rsa) order.add(lms.get(v));
        }
        return induce(s, k, t, order);
    }

    private static boolean same(int[] s, boolean[] t, int a, int b) {
        for (int d = 0;; d++) {
            boolean al = a + d < s.length && t[a + d] && !(a + d > 0 && t[a + d - 1]);
            boolean bl = b + d < s.length && t[b + d] && !(b + d > 0 && t[b + d - 1]);
            if (s[a + d] != s[b + d] || al != bl) return false;
            if (d > 0 && al && bl) return true;
        }
    }

    private static int[] induce(int[] s, int k, boolean[] t, List<Integer> lms) {
        int n = s.length;
        int[] sa = new int[n];
        Arrays.fill(sa, -1);

        int[] cnt = new int[k];
        for (int v : s) cnt[v]++;

        int[] end = cnt.clone();
        for (int i = 1; i < k; i++) end[i] += end[i - 1];
        for (int i = lms.size() - 1; i >= 0; i--) sa[--end[s[lms.get(i)]]] = lms.get(i);

        int[] start = new int[k];
        for (int i = 1; i < k; i++) start[i] = start[i - 1] + cnt[i - 1];
        for (int i = 0; i < n; i++) {
            int v = sa[i] - 1;
            if (v >= 0 && !t[v]) sa[start[s[v]]++] = v;
        }

        end = cnt.clone();
        for (int i = 1; i < k; i++) end[i] += end[i - 1];
        for (int i = n - 1; i >= 0; i--) {
            int v = sa[i] - 1;
            if (v >= 0 && t[v]) sa[--end[s[v]]] = v;
        }
        return sa;
    }

    public static int[] kasai(String s, int[] sa) {
        int n = s.length(), h = 0;
        int[] rank = new int[n], lcp = new int[n];
        for (int i = 0; i < n; i++) rank[sa[i]] = i;
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = sa[rank[i] - 1];
                while (i + h < n && j + h < n && s.charAt(i + h) == s.charAt(j + h)) h++;
                lcp[rank[i]] = h;
                if (h > 0) h--;
            }
        }
        return lcp;
    }

    public static final class SuffixAutomaton {
        static class S {
            Map<Character, Integer> next = new HashMap<>();
            int link = -1, len;
        }

        List<S> st = new ArrayList<>(List.of(new S()));
        int last = 0;

        public SuffixAutomaton(String text) {
            for (char c : text.toCharArray()) extend(c);
        }

        void extend(char c) {
            int cur = st.size();
            st.add(new S());
            st.get(cur).len = st.get(last).len + 1;
            int p = last;
            while (p >= 0 && !st.get(p).next.containsKey(c)) {
                st.get(p).next.put(c, cur);
                p = st.get(p).link;
            }
            if (p < 0) st.get(cur).link = 0;
            else {
                int q = st.get(p).next.get(c);
                if (st.get(p).len + 1 == st.get(q).len) st.get(cur).link = q;
                else {
                    int clone = st.size();
                    S z = new S();
                    z.next.putAll(st.get(q).next);
                    z.link = st.get(q).link;
                    z.len = st.get(p).len + 1;
                    st.add(z);
                    while (p >= 0 && st.get(p).next.get(c) == q) {
                        st.get(p).next.put(c, clone);
                        p = st.get(p).link;
                    }
                    st.get(q).link = st.get(cur).link = clone;
                }
            }
            last = cur;
        }

        public boolean contains(String q) {
            int v = 0;
            for (char c : q.toCharArray()) {
                Integer n = st.get(v).next.get(c);
                if (n == null) return false;
                v = n;
            }
            return true;
        }
    }
}
