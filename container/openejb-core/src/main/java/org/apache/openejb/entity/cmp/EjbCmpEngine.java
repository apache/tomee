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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.entity.cmp;

import java.util.Set;
import javax.ejb.DuplicateKeyException;
import javax.ejb.RemoveException;

import org.apache.openejb.transaction.EjbTransactionContext;
import org.apache.openejb.transaction.CmpTxData;

/**
 * @version $Revision$ $Date$
 */
public interface EjbCmpEngine {
    /**
     * Gets the all of the field accessor objects for both CMP and CMR fields.
     * @return the field accessors
     */
    Set getCmpFields();

    /**
     * Gets all of the select qureies for both the ejbSelect and finders.
     * @return the select queries
     */
    Set getSelectQueries();

    /**
     * Initialized an instance context before the ejbCreate callback is invoked.
     */
    void beforeCreate(CmpInstanceContext ctx);

    /**
     * Defines the primary key after the ejbCreate callback has been invoked.  After this method is invoked the instance
     * context will have an id set.
     */
    void afterCreate(CmpInstanceContext ctx, EjbTransactionContext ejbTransactionContext) throws DuplicateKeyException, Exception;

    /**
     * Removes the instance and handles cascade delete.  After this method returns the instance context will not have
     * an id set, nor will it contain any cmp data.
     */
    void afterRemove(CmpInstanceContext ctx, EjbTransactionContext ejbTransactionContext) throws RemoveException;

    void beforeLoad(CmpInstanceContext ctx) throws Exception;

    void afterStore(CmpInstanceContext ctx) throws Exception;

    CmpTxData createCmpTxData();
}
