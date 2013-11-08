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
import java.util.List;

/**
 * Tests the {@link RangeResultObjectProvider}.
 *
 * @author Abe White
 */
public class TestRangeResultObjectProvider extends ResultListTest {

    public TestRangeResultObjectProvider(String test) {
        super(test);
    }

    protected ResultList getResultList(ResultObjectProvider provider) {
        return new WindowResultList(provider, 10);
    }

    protected ResultObjectProvider[] getResultObjectProviders(List list) {
        // test 3 ranges:
        // 1. 0 to infinite
        // 2. 0 to N
        // 3. N to N + X
        ResultObjectProvider[] ranges = new ResultObjectProvider[3];
        ranges[0] = new RangeResultObjectProvider
            (new ListResultObjectProvider(list), 0, Integer.MAX_VALUE);

        List copy = new ArrayList(list.size() + 10);
        copy.addAll(list);
        for (int i = list.size(); i < list.size() + 10; i++)
            copy.add(String.valueOf(i));
        ranges[1] = new RangeResultObjectProvider
            (new ListResultObjectProvider(copy), 0, list.size());

        copy = new ArrayList(list.size() + 20);
        for (int i = -10; i < 0; i++)
            copy.add(String.valueOf(i));
        copy.addAll(list);
        for (int i = list.size(); i < list.size() + 10; i++)
            copy.add(String.valueOf(i));
        ranges[2] = new RangeResultObjectProvider
            (new ListResultObjectProvider(copy), 10, list.size() + 10);

        return ranges;
    }

    public static void main(String[] args) {
        main();
    }
}
