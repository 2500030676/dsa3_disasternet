package edu.disasternet.algorithms.dp;

import java.util.*;

/**
 * DP tools for noisy field text, supply package planning and analytics pipelines.
 * Edit distance uses O(mn) time/space; 0/1 resource selection uses O(nB)
 * time/space and reconstructs an optimal package; matrix-chain planning uses
 * O(n^3) time/O(n^2) space. B is the integer planning budget.
 */
public final class DynamicProgramming {

    private DynamicProgramming() {}

    public static int levenshtein(String a, String b) {
        return edit(a, b, false, null);
    }

    public static int damerauLevenshtein(String a, String b) {
        return edit(a, b, true, null);
    }

    public static int weighted(String a, String b, Map<Character, Integer> cost) {
        return edit(a, b, false, cost);
    }

    private static int edit(String a, String b, boolean swap, Map<Character, Integer> cost) {
        int[][] d = new int[a.length() + 1][b.length() + 1];

        for (int i = 1; i <= a.length(); i++) {
            d[i][0] = d[i - 1][0] + (cost == null ? 1 : cost.getOrDefault(a.charAt(i - 1), 1));
        }
        for (int j = 1; j <= b.length(); j++) {
            d[0][j] = d[0][j - 1] + (cost == null ? 1 : cost.getOrDefault(b.charAt(j - 1), 1));
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int c = a.charAt(i - 1) == b.charAt(j - 1) ? 0
                        : (cost == null ? 1 : cost.getOrDefault(b.charAt(j - 1), 1));
                int del = cost == null ? 1 : cost.getOrDefault(a.charAt(i - 1), 1);
                int ins = cost == null ? 1 : cost.getOrDefault(b.charAt(j - 1), 1);

                d[i][j] = Math.min(
                    Math.min(d[i - 1][j] + del, d[i][j - 1] + ins),
                    d[i - 1][j - 1] + c
                );

                if (swap && i > 1 && j > 1
                        && a.charAt(i - 1) == b.charAt(j - 2)
                        && a.charAt(i - 2) == b.charAt(j - 1)) {
                    d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + 1);
                }
            }
        }
        return d[a.length()][b.length()];
    }

    public record Item(String name, int cost, int value) {}
    public record Selection(List<Item> items, int cost, int value) {}

    public static Selection resourceSelection(List<Item> x, int budget) {
        if (budget < 0) throw new IllegalArgumentException("Budget cannot be negative.");
        int n = x.size();
        int[][] dp = new int[n + 1][budget + 1];
        for (int i = 0; i < n; i++) {
            Item item = x.get(i);
            if (item.cost() < 0) throw new IllegalArgumentException("Item cost cannot be negative.");
            for (int b = 0; b <= budget; b++) {
                dp[i + 1][b] = dp[i][b];
                if (item.cost() <= b) dp[i + 1][b] = Math.max(dp[i + 1][b], dp[i][b - item.cost()] + item.value());
            }
        }

        int b = budget;
        List<Item> out = new ArrayList<>();
        for (int i = n; i > 0; i--) {
            if (dp[i][b] != dp[i - 1][b]) {
                Item item = x.get(i - 1);
                out.add(item);
                b -= item.cost();
            }
        }
        Collections.reverse(out);
        int cost = out.stream().mapToInt(Item::cost).sum();
        return new Selection(List.copyOf(out), cost, dp[n][budget]);
    }

    public record MatrixPlan(long cost, String order) {}

    public static MatrixPlan matrixChain(int[] p) {
        int n = p.length - 1;
        long[][] m = new long[n][n];
        int[][] s = new int[n][n];

        for (int l = 2; l <= n; l++) {
            for (int i = 0; i <= n - l; i++) {
                int j = i + l - 1;
                m[i][j] = Long.MAX_VALUE;

                for (int k = i; k < j; k++) {
                    long q = m[i][k] + m[k + 1][j] + (long) p[i] * p[k + 1] * p[j + 1];
                    if (q < m[i][j]) {
                        m[i][j] = q;
                        s[i][j] = k;
                    }
                }
            }
        }
        return new MatrixPlan(m[0][n - 1], order(s, 0, n - 1));
    }

    private static String order(int[][] s, int i, int j) {
        return i == j
            ? "A" + (i + 1)
            : "(" + order(s, i, s[i][j]) + " * " + order(s, s[i][j] + 1, j) + ")";
    }
}
