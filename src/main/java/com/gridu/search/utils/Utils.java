package com.gridu.search.utils;

import java.util.Arrays;

public class Utils {
    public static int damerauLevenshteinDistance(String a, String b) {
        return damerauLevenshteinDistance(a, b, a.length() - 1, b.length() - 1);
    }

    private static int damerauLevenshteinDistance(String a, String b, int i, int j) {
        if (Math.min(i, j) == 0) {
            return Math.max(i, j);
        }

        if (i > 1 && j > 1 && a.charAt(i) == b.charAt(j - 1) && a.charAt(i - 1) == b.charAt(j)) {
            return min(
                    damerauLevenshteinDistance(a, b, i - 1, j) + 1,
                    damerauLevenshteinDistance(a, b, i, j - 1) + 1,
                    damerauLevenshteinDistance(a, b, i - 1, j - 1) + (a.charAt(i) == b.charAt(j) ? 0 : 1),
                    damerauLevenshteinDistance(a, b, i - 2, j - 2) + 1
            );
        }

        return min(
                damerauLevenshteinDistance(a, b, i - 1, j) + 1,
                damerauLevenshteinDistance(a, b, i, j - 1) + 1,
                damerauLevenshteinDistance(a, b, i - 1, j - 1) + (a.charAt(i) == b.charAt(j) ? 0 : 1)
        );
    }

    private static int min(int... values) {
        if (values.length == 0) {
            return Integer.MAX_VALUE;
        }

        return Arrays.stream(values).min().getAsInt();
    }
}
