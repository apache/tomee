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
package org.apache.openjpa.lib.rop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Tests the {@link MergedResultObjectProvider}.
 *
 * @author Abe White
 */
public class TestOrderedMergedResultObjectProvider extends ResultListTest {

    public TestOrderedMergedResultObjectProvider(String test) {
        super(test);
    }

    protected ResultList getResultList(ResultObjectProvider provider) {
        return new WindowResultList(provider, 10);
    }

    protected ResultObjectProvider[] getResultObjectProviders(List list) {
        Collections.shuffle(list);
        int quart = list.size() / 4;
        List list1 = new ArrayList(list.subList(0, quart));
        List list2 = new ArrayList(list.subList(quart, quart * 2));
        List list3 = new ArrayList(list.subList(quart * 2, quart * 3));
        List list4 = new ArrayList(list.subList(quart * 3, list.size()));

        Comparator comp = new IntValueComparator();
        Collections.sort(list1, comp);
        Collections.sort(list2, comp);
        Collections.sort(list3, comp);
        Collections.sort(list4, comp);

        ResultObjectProvider[] rops = new ResultObjectProvider[]{
            new ListResultObjectProvider(list1),
            new ListResultObjectProvider(list2),
            new ListResultObjectProvider(list3),
            new ListResultObjectProvider(list4), };
        return new ResultObjectProvider[]{
            new MergedResultObjectProvider(rops, comp)
        };
    }

    public static void main(String[] args) {
        main();
    }

    private static class IntValueComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            return Integer.valueOf(o1.toString()).
                compareTo(Integer.valueOf(o2.toString()));
        }
    }
}
