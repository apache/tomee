/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.lib.util;

import java.util.Arrays;
import java.util.Collection;

/**
 * Utilities for calculating string distance.
 *
 * @author Marc Prud'hommeaux
 * @nojavadoc
 */
public class StringDistance {

    /**
     * Returns the candidate string with the closest Levenshtein distance
     * to the given string.
     *
     * @see #getClosestLevenshteinDistance(String,Collection,int)
     */
    public static String getClosestLevenshteinDistance(String str,
        String[] candidates) {
        if (candidates == null)
            return null;
        return getClosestLevenshteinDistance(str, Arrays.asList(candidates));
    }

    /**
     * Returns the candidate string with the closest Levenshtein distance
     * to the given string.
     *
     * @see #getClosestLevenshteinDistance(String,Collection,int)
     */
    public static String getClosestLevenshteinDistance(String str,
        Collection candidates) {
        return getClosestLevenshteinDistance(str, candidates,
            Integer.MAX_VALUE);
    }

    /**
     * Returns the candidate string with the closest Levenshtein distance
     * to the given string.
     *
     * @see #getClosestLevenshteinDistance(String,Collection,int)
     */
    public static String getClosestLevenshteinDistance(String str,
        String[] candidates, int threshold) {
        if (candidates == null)
            return null;
        return getClosestLevenshteinDistance(str, Arrays.asList(candidates),
            threshold);
    }

    /**
     * Returns the candidate string with the closest Levenshtein distance
     * to the given string and using the threshold as the specified
     * percentage of the length of the candidate string(0.0f-1.0f).
     *
     * @see #getClosestLevenshteinDistance(String,Collection,int)
     */
    public static String getClosestLevenshteinDistance(String str,
        String[] candidates, float thresholdPercentage) {
        if (candidates == null)
            return null;

        return getClosestLevenshteinDistance(str, Arrays.asList(candidates),
            thresholdPercentage);
    }

    /**
     * Returns the candidate string with the closest Levenshtein distance
     * to the given string and using the threshold as the specified
     * percentage of the length of the candidate string(0.0f-1.0f).
     *
     * @see #getClosestLevenshteinDistance(String,Collection,int)
     */
    public static String getClosestLevenshteinDistance(String str,
        Collection candidates, float thresholdPercentage) {
        if (str == null)
            return null;

        thresholdPercentage = Math.min(thresholdPercentage, 1.0f);
        thresholdPercentage = Math.max(thresholdPercentage, 0.0f);

        return getClosestLevenshteinDistance(str, candidates,
            (int) (str.length() * thresholdPercentage));
    }

    /**
     * Returns the candidate string with the closest Levenshtein distance
     * to the given string.
     *
     * @param str the string to check
     * @param candidates the list of strings to test against
     * @param threshold the threshold distance a candidate must meet
     * @see #getLevenshteinDistance
     */
    public static String getClosestLevenshteinDistance(String str,
        Collection<String> candidates, int threshold) {
        if (candidates == null || candidates.isEmpty())
            return null;

        String minString = null;
        int minValue = Integer.MAX_VALUE;

        for(String candidate : candidates) { 
            int distance = getLevenshteinDistance(str, candidate);
            if (distance < minValue) {
                minValue = distance;
                minString = candidate;
            }
        }

        // return the lowest close string only if we surpass the threshhold
        if (minValue <= threshold)
            return minString;
        else
            return null;
    }

    /**
     * Returns the Levenshtein distance between the two strings.
     * The distance is the minimum number of changes that need to be
     * applied to the first string in order to get to the second
     * string. For details of the algorithm, see
     * <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">
     * http://en.wikipedia.org/wiki/Levenshtein_distance</a>.
     */
    public static int getLevenshteinDistance(String s, String t) {
        int n = s.length();
        int m = t.length();

        if (n == 0)
            return m;

        if (m == 0)
            return n;

        int[][] matrix = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++)
            matrix[i][0] = i;

        for (int j = 0; j <= m; j++)
            matrix[0][j] = j;

        for (int i = 1; i <= n; i++) {
            int si = s.charAt(i - 1);

            for (int j = 1; j <= m; j++) {
                int tj = t.charAt(j - 1);

                int cost;

                if (si == tj)
                    cost = 0;
                else
                    cost = 1;

                matrix[i][j] = min(matrix[i - 1][j] + 1, matrix[i][j - 1] + 1,
                    matrix[i - 1][j - 1] + cost);
            }
        }

        return matrix[n][m];
    }

    private static int min(int a, int b, int c) {
        int mi = a;

        if (b < mi)
            mi = b;

        if (c < mi)
            mi = c;

        return mi;
    }
}

