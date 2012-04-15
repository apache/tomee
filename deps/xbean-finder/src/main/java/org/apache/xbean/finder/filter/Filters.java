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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class Filters {
    private static final Filter NONE = new Filter() {
        public boolean accept(String name) {
            return false;
        }
    };

    public static Filter packages(String... packages) {
        List<Filter> filters = new ArrayList<Filter>();
        for (String s : packages) {
            filters.add(new PackageFilter(s));
        }

        return optimize(filters);
    }

    public static Filter classes(String... classes) {
        List<Filter> filters = new ArrayList<Filter>();
        for (String s : classes) {
            filters.add(new ClassFilter(s));
        }

        return optimize(filters);
    }

    public static Filter prefixes(String... prefixes) {
        List<Filter> filters = new ArrayList<Filter>();
        for (String s : prefixes) {
            filters.add(new PrefixFilter(s));
        }

        return optimize(filters);
    }

    public static Filter tokens(String... tokens) {
        List<Filter> filters = new ArrayList<Filter>();
        for (String s : tokens) {
            filters.add(new ContainsFilter(s));
        }

        return optimize(filters);
    }

    public static Filter suffixes(String... suffixes) {
        List<Filter> filters = new ArrayList<Filter>();
        for (String s : suffixes) {
            filters.add(new SuffixFilter(s));
        }

        return optimize(filters);
    }

    public static Filter patterns(String... patterns) {
        List<Filter> filters = new ArrayList<Filter>();
        for (String s : patterns) {
            filters.add(new PatternFilter(s));
        }

        return optimize(filters);
    }


    public static Filter optimize(Filter... filters) {
        return optimize(Arrays.asList(filters));
    }

    public static Filter optimize(List<Filter>... filterss) {
        Set<Filter> unwrapped = new LinkedHashSet<Filter>();

        for (List<Filter> filters : filterss) {
            unwrap(filters, unwrapped);
        }

        if (unwrapped.size() > 1) {
            Iterator<Filter> iterator = unwrapped.iterator();
            while (iterator.hasNext()) {
                Filter filter = iterator.next();
                if (filter == NONE) iterator.remove();
            }
        }

        if (unwrapped.size() == 0) return NONE;
        if (unwrapped.size() == 1) return unwrapped.iterator().next();
        return new FilterList(unwrapped);
    }

    /**
     * Will invert the meaning of this filter by wrapping it with
     * a filter that negates the return of the accept method.
     *
     * If the passed in filter is already wrapped, it will be
     * unwrapped and returned.  This is to prevent endless wrapping
     * if the invert method is called many times.
     * 
     * @param filter
     * @return
     */
    public static Filter invert(Filter filter) {
        if (filter instanceof NegativeFilter) {
            NegativeFilter negativeFilter = (NegativeFilter) filter;
            return negativeFilter.getFilter();
        }

        return new NegativeFilter(filter);
    }

    private static void unwrap(List<Filter> filters, Set<Filter> unwrapped) {
        for (Filter filter : filters) {
            if (filter instanceof FilterList) {
                FilterList filterList = (FilterList) filter;
                unwrap(filterList.getFilters(), unwrapped);
            } else {
                unwrapped.add(filter);
            }
        }
    }

    private static final class NegativeFilter implements Filter {
        private final Filter filter;

        public NegativeFilter(Filter filter) {
            this.filter = filter;
        }

        public boolean accept(String name) {
            return !filter.accept(name);
        }

        public Filter getFilter() {
            return filter;
        }
    }

}
