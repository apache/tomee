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

import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;

import org.tranql.builder.IdentityDefinerBuilder;
import org.tranql.ejb.EJBQLQuery;
import org.tranql.field.FieldTransform;
import org.tranql.field.FieldTransformException;
import org.tranql.field.Row;
import org.tranql.ql.QueryException;
import org.tranql.query.QueryCommand;
import org.tranql.query.ResultHandler;
import org.tranql.schema.Entity;

/**
 * 
 * 
 * @version $Revision$ $Date$
 */
public class TranqlSingleValuedQuery extends TranqlSelectQuery {
    private static final Object NODATA = new Object();

    public TranqlSingleValuedQuery(EJBQLQuery ejbqlQuery, QueryCommand localCommand, QueryCommand remoteCommand, Entity selectedEntity, IdentityDefinerBuilder idDefinerBuilder) {
        super(ejbqlQuery, localCommand, remoteCommand, selectedEntity, idDefinerBuilder);
    }

    public Class getReturnType() {
        return Object.class;
    }

    public Object execute(CmpInstanceContext ctx, Object[] args, boolean local) throws FinderException {
        Object result;
        try {
            ResultHandler handler = new SingleValuedResultHandler(getResultAccessor(local));
            result = execute(ctx, handler, args, NODATA, local);
        } catch (QueryException e) {
            throw (FinderException) new FinderException(e.getMessage()).initCause(e);
        }
        if (NODATA == result) {
            throw new ObjectNotFoundException();
        }
        return result;
    }

    private class SingleValuedResultHandler implements ResultHandler {
        private final FieldTransform accessor;
        public SingleValuedResultHandler(FieldTransform accessor) {
            this.accessor = accessor;
        }

        public Object fetched(Row row, Object arg) throws QueryException {
            if (arg == NODATA) {
                try {
                    return accessor.get(row);
                } catch (FieldTransformException e) {
                    throw new QueryException(e);
                }
            }
            throw new QueryException("More than one row returned from single valued select.");
        }
        
        public Object endFetched(Object arg0) throws QueryException {
            return arg0;
        }
    }
}
