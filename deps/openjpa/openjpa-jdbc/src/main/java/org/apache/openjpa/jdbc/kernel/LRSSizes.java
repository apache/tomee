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
package org.apache.openjpa.jdbc.kernel;

import java.sql.ResultSet;

/**
 * Ways of calculating the size of large result sets.
 *
 * @author Abe White
 */
public interface LRSSizes {

    /**
     * Mode for returning {@link Integer#MAX_VALUE} for the size of
     * large result sets.
     */
    public static final int SIZE_UNKNOWN = 0;

    /**
     * Mode for using {@link ResultSet#last} to calcualte the size of
     * large result sets.
     */
    public static final int SIZE_LAST = 1;

    /**
     * Mode for using a query to calculate the size of large result sets.
     */
    public static final int SIZE_QUERY = 2;
}
