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

import org.tranql.cache.AlreadyAssociatedException;
import org.tranql.cache.CacheRow;
import org.tranql.cache.InTxCache;
import org.tranql.field.Row;
import org.tranql.identity.GlobalIdentity;
import org.tranql.identity.IdentityDefiner;
import org.tranql.identity.UndefinedIdentityException;
import org.tranql.ql.QueryException;
import org.tranql.query.ResultHandler;

/**
 * 
 * 
 * @version $Revision$ $Date$
 */
class CacheFiller implements ResultHandler {
    private final ResultHandler delegate;
    private final IdentityDefiner idDefiner;
    private final IdentityDefiner idInjector;
    private final InTxCache cache;
    
    public CacheFiller(ResultHandler delegate, IdentityDefiner idDefiner, IdentityDefiner idInjector, InTxCache cache) {
        this.delegate = delegate;
        this.idDefiner = idDefiner;
        this.idInjector = idInjector;
        this.cache = cache;
    }

    public Object fetched(Row row, Object arg) throws QueryException {
        try {
            GlobalIdentity id = idDefiner.defineIdentity(row);
            if (null != id && null == cache.get(id)) {
                CacheRow cacheRow = id.getTable().emptyRow(id);
                idInjector.injectIdentity(cacheRow);
                cache.add(cacheRow);
            }
        } catch (UndefinedIdentityException e) {
            throw new QueryException(e);
        } catch (AlreadyAssociatedException e) {
            throw new QueryException(e);
        }
        return delegate.fetched(row, arg);
    }

    public Object endFetched(Object arg) throws QueryException {
        return delegate.endFetched(arg);
    }

}
