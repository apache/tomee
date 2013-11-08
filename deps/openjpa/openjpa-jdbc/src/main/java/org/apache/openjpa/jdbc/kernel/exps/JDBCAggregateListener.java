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
package org.apache.openjpa.jdbc.kernel.exps;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.kernel.exps.AggregateListener;

/**
 * JDBC extension to the {@link AggregateListener}.
 *
 * @author Abe White
 */
public interface JDBCAggregateListener
    extends AggregateListener {

    /**
     * Append the SQL for this aggregate.
     *
     * @param buf the SQL buffer to append to
     * @param args the values of the arguments given in the filter, or
     * null if this listener doesn't expect arguments
     * @param mapping the class mapping for the query's candidate class
     * @param store the store that owns the query
     */
    public void appendTo(SQLBuffer buf, FilterValue[] args,
        ClassMapping mapping, JDBCStore store);
}
