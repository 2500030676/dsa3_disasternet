package edu.disasternet.algorithms.randomparallel;

import java.util.*;

/**
 * Randomized stream/ranking methods and explicit parallel-work demonstrations.
 * Randomized quicksort is expected O(n log n), worst O(n^2), with expected
 * O(log n) stack; reservoir sampling is O(n) time/O(k) space; Miller-Rabin
 * uses deterministic bases for signed 64-bit inputs; Blelloch scan and reduce
 * perform O(n) work, O(log n) span and O(n) storage. Methods expose traces so
 * the CLI can explain the actual computation on live DisasterNet records.
 */
public final class RandomParallelAlgorithms {

    private RandomParallelAlgorithms() {}

    public record SortTrace(List<Integer> sorted, int comparisons, int swaps, List<Integer> pivots) {}

    public static SortTrace randomizedQuickSort(List<Integer> a, long seed) {
        List<Integer> x = new ArrayList<>(a), p = new ArrayList<>();
        int[] ops = new int[2];
        qs(x, 0, x.size() - 1, new Random(seed), p, ops);
        return new SortTrace(x, ops[0], ops[1], p);
    }

    static void qs(List<Integer> a, int l, int r, Random z, List<Integer> p, int[] o) {
        if (l >= r) return;
        int q = l + z.nextInt(r - l + 1);
        p.add(a.get(q));
        Collections.swap(a, q, r);
        o[1]++;
        int i = l;
        for (int j = l; j < r; j++) {
            o[0]++;
            if (a.get(j) <= a.get(r)) {
                Collections.swap(a, i++, j);
                o[1]++;
            }
        }
        Collections.swap(a, i, r);
        o[1]++;
        qs(a, l, i - 1, z, p, o);
        qs(a, i + 1, r, z, p, o);
    }

    public static <T> List<T> reservoirSample(Iterable<T> stream, int k, long seed) {
        List<T> r = new ArrayList<>();
        Random z = new Random(seed);
        int n = 0;
        for (T v : stream) {
            n++;
            if (r.size() < k) r.add(v);
            else {
                int j = z.nextInt(n);
                if (j < k) r.set(j, v);
            }
        }
        return r;
    }

    public static boolean millerRabin(long n) {
        if (n < 2) return false;
        for (long p : new long[]{2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37}) {
            if (n == p) return true;
            if (n % p == 0) return false;
        }
        long d = n - 1;
        int s = 0;
        while ((d & 1) == 0) {
            d >>= 1;
            s++;
        }
        for (long a : new long[]{2, 325, 9375, 28178, 450775, 9780504, 1795265022}) {
            if (a % n == 0) continue;
            long x = pow(a % n, d, n);
            if (x == 1 || x == n - 1) continue;
            int r = 1;
            while (r++ < s && x != n - 1) x = mul(x, x, n);
            if (x != n - 1) return false;
        }
        return true;
    }

    static long mul(long a, long b, long m) {
        long r = 0;
        while (b > 0) {
            if ((b & 1) > 0) r = addMod(r, a, m);
            a = addMod(a, a, m);
            b >>= 1;
        }
        return r;
    }

    /** Adds residues without overflowing a signed long, including for moduli near Long.MAX_VALUE. */
    private static long addMod(long a, long b, long modulus) {
        return a >= modulus - b ? a - (modulus - b) : a + b;
    }

    static long pow(long a, long b, long m) {
        long r = 1;
        while (b > 0) {
            if ((b & 1) > 0) r = mul(r, a, m);
            a = mul(a, a, m);
            b >>= 1;
        }
        return r;
    }

    public record ScanResult(int[] exclusive, int[] inclusive, List<String> stages) {}

    public static ScanResult blellochScan(int[] a) {
        int n = 1;
        while (n < a.length) n <<= 1;
        int[] x = new int[n];
        System.arraycopy(a, 0, x, 0, a.length);
        List<String> s = new ArrayList<>();

        for (int d = 1; d < n; d <<= 1) {
            for (int i = 0; i < n; i += 2 * d) x[i + 2 * d - 1] += x[i + d - 1];
            s.add("upsweep d=" + d + ": " + Arrays.toString(x));
        }

        x[n - 1] = 0;

        for (int d = n >> 1; d >= 1; d >>= 1) {
            for (int i = 0; i < n; i += 2 * d) {
                int t = x[i + d - 1];
                x[i + d - 1] = x[i + 2 * d - 1];
                x[i + 2 * d - 1] += t;
            }
            s.add("downsweep d=" + d + ": " + Arrays.toString(x));
        }

        int[] ex = Arrays.copyOf(x, a.length), in = new int[a.length];
        for (int i = 0; i < a.length; i++) in[i] = ex[i] + a[i];
        return new ScanResult(ex, in, s);
    }

    public static List<List<Integer>> reductionTree(int[] a) {
        List<List<Integer>> tree = new ArrayList<>();
        List<Integer> cur = new ArrayList<>();
        for (int x : a) cur.add(x);
        tree.add(cur);

        while (cur.size() > 1) {
            List<Integer> n = new ArrayList<>();
            for (int i = 0; i < cur.size(); i += 2)
                n.add(cur.get(i) + (i + 1 < cur.size() ? cur.get(i + 1) : 0));
            cur = n;
            tree.add(cur);
        }
        return tree;
    }

    /** Uses Java's parallel stream reduction over the current worker pool. */
    public static int parallelReduce(int[] values) {
        return Arrays.stream(values).parallel().sum();
    }

    public static double brentEstimate(long work, long span, int processors) {
        return (double) work / processors + span;
    }
}
