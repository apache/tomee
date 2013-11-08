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
package org.apache.openjpa.kernel;

import org.apache.openjpa.conf.OpenJPAConfiguration;

/**
 * Constants for determining when to flush before queries.
 *
 * @author Abe White
 * @since 0.4.0
 */
public interface QueryFlushModes {

    /**
     * Constant denoting that queries should flush data to the
     * database automatically whenever OpenJPA determines that relevant
     * modifications have been made and IgnoreCache is
     * <code>false</code>. See 
     * {@link OpenJPAConfiguration#getFlushBeforeQueries}
     * for more info.
     */
    public static final int FLUSH_TRUE = 0;

    /**
     * Constant denoting that queries should never flush data
     * to the database automatically before executing a query, but
     * should instead execute queries in-memory if necessary. See
     * {@link OpenJPAConfiguration#getFlushBeforeQueries} for more info.
     */
    public static final int FLUSH_FALSE = 1;

    /**
     * Constant denoting that queries should flush data to the
     * database automatically when OpenJPA determines that relevant
     * modifications have been made and the current
     * context already has a dedicated connection. See
     * {@link OpenJPAConfiguration#getFlushBeforeQueries} for more info.
     */
    public static final int FLUSH_WITH_CONNECTION = 2;
}
