/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.xbean.finder.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class FilterList implements Filter {

    private final List<Filter> filters = new ArrayList<Filter>();

    public FilterList(Filter... filters) {
        this(Arrays.asList(filters));
    }

    public FilterList(Iterable<Filter> filters) {
        for (Filter filter : filters) {
            this.filters.add(filter);
        }
    }

    public boolean accept(String name) {
        for (Filter filter : filters) {
            if (filter.accept(name)) return true;
        }

        return false;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    @Override
    public String toString() {
        return "FilterList{" +
                "filters=" + filters +
                '}';
    }
}
