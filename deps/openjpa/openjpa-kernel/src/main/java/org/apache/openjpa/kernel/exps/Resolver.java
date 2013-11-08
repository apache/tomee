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
package org.apache.openjpa.kernel.exps;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.QueryContext;

/**
 * A Resolver is used to resolve listeners and class or entity names
 * that appear in a query.
 *
 * @author Abe White
 * @nojavadoc
 */
public interface Resolver {

    /**
     * Resolve the type represented by the given class name. This will
     * test the type against the namespace of the Query and the declared
     * imports, and will properly handle primitives and java.lang types
     * as well. Returns null if the name does not match a known type.
     */
    public Class classForName(String name, String[] imports);

    /**
     * Return the filter listener for the given tag, or null if none.
     */
    public FilterListener getFilterListener(String tag);

    /**
     * Return the function listener for the given tag, or null if none.
     */
    public AggregateListener getAggregateListener(String tag);

    /**
     * Return the OpenJPA configuration.
     */
    public OpenJPAConfiguration getConfiguration ();

    /**
     * The {@link QueryContext} for which this resolver was created
     *
     * @since 1.1.0
     */
    public QueryContext getQueryContext();
}
