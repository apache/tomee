/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.openjpa.persistence.query;

/**
 * SelectItem instances are used in specifying the query's select list.
 * <p/>
 * The methods of this interface are used to define arguments that can be passed
 * to the orderBy method for use in ordering selected items of the query result.
 */
public interface SelectItem extends OrderByItem {
    /**
     * Return an OrderByItem referencing the SelectItem and specifying ascending
     * ordering. The SelectItem must correspond to an orderable value.
     *
     * @return order-by item
     */
    OrderByItem asc();

    /**
     * Return an OrderByItem referencing the SelectItem and specifying
     * descending ordering. The SelectItem must correspond to an orderable
     * value.
     *
     * @return order-by item
     */
    OrderByItem desc();
}
