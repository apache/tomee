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

import java.io.Serializable;
import javax.ejb.FinderException;

import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.dispatch.VirtualOperation;

/**
 * @version $Revision$ $Date$
 */
public class CmpFinder implements VirtualOperation, Serializable {
    private static final long serialVersionUID = 7214541579887196921L;
    private final SelectQuery selectQuery;

    public CmpFinder(SelectQuery selectQuery) {
        this.selectQuery = selectQuery;
    }

    public InvocationResult execute(EjbInvocation invocation) throws Throwable {
        try {
            boolean local = invocation.getType().isLocal();
            CmpInstanceContext ctx = (CmpInstanceContext) invocation.getEJBInstanceContext();
            Object[] arguments = invocation.getArguments();
            Object results = selectQuery.execute(ctx, arguments, local);
            return invocation.createResult(results);
        } catch (FinderException e) {
            return invocation.createExceptionResult(e);
        }
    }
}