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

import org.tranql.builder.IdentityDefinerBuilder;
import org.tranql.cache.InTxCache;
import org.tranql.ejb.EJBQLQuery;
import org.tranql.field.FieldTransform;
import org.tranql.field.Row;
import org.tranql.identity.IdentityDefiner;
import org.tranql.ql.Query;
import org.tranql.ql.QueryException;
import org.tranql.query.QueryCommand;
import org.tranql.query.ResultHandler;
import org.tranql.schema.Entity;
import org.apache.openejb.transaction.EjbTransactionContext;

public abstract class TranqlSelectQuery implements SelectQuery {
    private final EJBQLQuery ejbqlQuery;

    private final QueryCommand localCommand;
    private final QueryCommand remoteCommand;
    private final FieldTransform resultAccessor;
    private final IdentityDefiner idDefiner;
    private final IdentityDefiner idInjector;

    public TranqlSelectQuery(EJBQLQuery ejbqlQuery, QueryCommand localCommand, QueryCommand remoteCommand, Entity selectedEntity, IdentityDefinerBuilder identityDefinerBuilder) {
        this.ejbqlQuery = ejbqlQuery;
        this.localCommand = localCommand;
        this.remoteCommand = remoteCommand;

        if (selectedEntity != null) {
            idDefiner = identityDefinerBuilder.getIdentityDefiner(selectedEntity, 0);
            idInjector = identityDefinerBuilder.getIdentityDefiner(selectedEntity);
        } else {
            idDefiner = null;
            idInjector = null;
        }

        Query query = localCommand.getQuery();
        resultAccessor = query.getResultAccessors()[0];
    }

    public String getMethodName() {
        return ejbqlQuery.getMethodName();
    }

    public Class[] getParameterTypes() {
        return ejbqlQuery.getParameterTypes();
    }

    public FieldTransform getResultAccessor(boolean local) {
        if (local) {
            return localCommand.getQuery().getResultAccessors()[0];
        } else {
            return remoteCommand.getQuery().getResultAccessors()[0];
        }
    }

    protected Object execute(CmpInstanceContext ctx, ResultHandler resultHandler, Object[] args, Object results, boolean local) throws QueryException {
        EjbTransactionContext ejbTransactionData = ctx.getEjbTransactionData();
        if (ejbqlQuery.isFlushCacheBeforeQuery()) {
            try {
                ejbTransactionData.flush();
            } catch (QueryException e) {
                throw e;
            } catch (RuntimeException e) {
                throw e;
            } catch (Error e) {
                throw e;
            } catch (Throwable throwable) {
                throw new QueryException("Exception while flushing cmp data", throwable);
            }
        }

        QueryCommand command;
        if (local) {
            command = localCommand;
        } else {
            command = remoteCommand;
        }

        InTxCache cache = (InTxCache) ejbTransactionData.getCmpTxData();
        resultHandler = new CacheFiller(resultHandler, idDefiner, idInjector, cache);
        return command.execute(cache, resultHandler, new Row(args), results);
    }
}