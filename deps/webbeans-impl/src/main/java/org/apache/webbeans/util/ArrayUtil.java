/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.util;

import java.util.HashSet;
import java.util.Set;

public final class ArrayUtil
{

    private ArrayUtil()
    {

    }

    /**
     * Compare two arrays regardless of the position of the elements
     * in the arrays. The complex handling with temporary flags is necessary due
     * to the possibility of having multiple occurrences of the same element in
     * the arrays. In this case both arrays have to contain the exactly same
     * amount of those elements. This is only suited for smaller arrays (e.g.
     * count < 100) since the algorithm uses a product of both arrays. If one
     * likes to use this for larger arrays, we'd have to use hashes.
     * 
     * @param arr1
     * @param arr2
     * @return
     */
    public static boolean equalsIgnorePosition(Object[] arr1, Object[] arr2)
    {
        if (arr1 == null && arr2 == null)
        {
            return true;
        }

        if (arr1 == null || arr2 == null)
        {
            return false;
        }

        if (arr1.length != arr2.length)
        {
            return false;
        }

        boolean[] found1 = new boolean[arr1.length];
        boolean[] found2 = new boolean[arr2.length];

        for (int i1 = 0; i1 < arr1.length; i1++)
        {
            Object o1 = arr1[i1];

            for (int i2 = 0; i2 < arr2.length; i2++)
            {
                Object o2 = arr2[i2];

                // if they are equal and not found already
                if (o1.equals(o2) && !found2[i2])
                {
                    // mark the entries in both arrays as found
                    found1[i1] = true;
                    found2[i2] = true;
                    break;
                }
            }
        }

        for (int i = 0; i < found1.length; i++)
        {
            if (!found1[i] || !found2[i])
            {
                return false;
            }
        }
        return true;
    }

    public static <T> Set<T> asSet(T... items)
    {
        Set<T> set = new HashSet<T>();

        for(T item : items)
        {
            set.add(item);
        }

        return set;
    }
}
