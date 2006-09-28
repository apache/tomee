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

import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.FinderException;

import org.tranql.builder.IdentityDefinerBuilder;
import org.tranql.ejb.EJBQLQuery;
import org.tranql.ql.QueryException;
import org.tranql.query.CollectionResultHandler;
import org.tranql.query.QueryCommand;
import org.tranql.query.ResultHandler;
import org.tranql.schema.Entity;


/**
 * 
 * 
 * @version $Revision$ $Date$
 */
public class TranqlCollectionValuedQuery extends TranqlSelectQuery {
    public TranqlCollectionValuedQuery(EJBQLQuery ejbqlQuery, QueryCommand localCommand, QueryCommand remoteCommand, Entity selectedEntity, IdentityDefinerBuilder idDefinerBuilder) {
        super(ejbqlQuery, localCommand, remoteCommand, selectedEntity, idDefinerBuilder);
    }

    public Class getReturnType() {
        return Collection.class;
    }

    public Object execute(CmpInstanceContext ctx, Object[] args, boolean local) throws FinderException {
        Collection results = new ArrayList();
        try {
            ResultHandler handler = new CollectionResultHandler(getResultAccessor(local));
            execute(ctx, handler, args, results, local);
        } catch (QueryException e) {
            throw (FinderException) new FinderException(e.getMessage()).initCause(e);
        }
        return results;
    }
}
