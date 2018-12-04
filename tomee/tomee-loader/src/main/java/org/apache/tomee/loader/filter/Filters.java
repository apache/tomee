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
package org.apache.tomee.loader.filter;

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
        public boolean accept(final String name) {
            return false;
        }
    };

    public static Filter packages(final String... packages) {
        final List<Filter> filters = new ArrayList<>();
        for (final String s : packages) {
            filters.add(new PackageFilter(s));
        }

        return optimize(filters);
    }

    public static Filter classes(final String... classes) {
        final List<Filter> filters = new ArrayList<>();
        for (final String s : classes) {
            filters.add(new ClassFilter(s));
        }

        return optimize(filters);
    }

    public static Filter prefixes(final String... prefixes) {
        final List<Filter> filters = new ArrayList<>();
        for (final String s : prefixes) {
            filters.add(new PrefixFilter(s));
        }

        return optimize(filters);
    }

    public static Filter tokens(final String... tokens) {
        final List<Filter> filters = new ArrayList<>();
        for (final String s : tokens) {
            filters.add(new ContainsFilter(s));
        }

        return optimize(filters);
    }

    public static Filter suffixes(final String... suffixes) {
        final List<Filter> filters = new ArrayList<>();
        for (final String s : suffixes) {
            filters.add(new SuffixFilter(s));
        }

        return optimize(filters);
    }

    public static Filter patterns(final String... patterns) {
        final List<Filter> filters = new ArrayList<>();
        for (final String s : patterns) {
            filters.add(new PatternFilter(s));
        }

        return optimize(filters);
    }


    public static Filter optimize(final Filter... filters) {
        return optimize(Arrays.asList(filters));
    }

    public static Filter optimize(final List<Filter>... filterss) {
        final Set<Filter> unwrapped = new LinkedHashSet<>();

        for (final List<Filter> filters : filterss) {
            unwrap(filters, unwrapped);
        }

        if (unwrapped.size() > 1) {
            final Iterator<Filter> iterator = unwrapped.iterator();
            while (iterator.hasNext()) {
                final Filter filter = iterator.next();
                if (filter == NONE) {
                    iterator.remove();
                }
            }
        }

        if (unwrapped.isEmpty()) {
            return NONE;
        }
        if (unwrapped.size() == 1) {
            return unwrapped.iterator().next();
        }
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
    public static Filter invert(final Filter filter) {
        if (filter instanceof NegativeFilter) {
            final NegativeFilter negativeFilter = (NegativeFilter) filter;
            return negativeFilter.getFilter();
        }

        return new NegativeFilter(filter);
    }

    private static void unwrap(final List<Filter> filters, final Set<Filter> unwrapped) {
        for (final Filter filter : filters) {
            if (filter instanceof FilterList) {
                final FilterList filterList = (FilterList) filter;
                unwrap(filterList.getFilters(), unwrapped);
            } else {
                unwrapped.add(filter);
            }
        }
    }

    private static final class NegativeFilter implements Filter {
        private final Filter filter;

        public NegativeFilter(final Filter filter) {
            this.filter = filter;
        }

        public boolean accept(final String name) {
            return !filter.accept(name);
        }

        public Filter getFilter() {
            return filter;
        }
    }

}
