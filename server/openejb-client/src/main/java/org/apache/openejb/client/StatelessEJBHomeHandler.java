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
package org.apache.openejb.client;

import jakarta.ejb.RemoveException;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;

public class StatelessEJBHomeHandler extends EJBHomeHandler {

    public StatelessEJBHomeHandler() {
    }

    public StatelessEJBHomeHandler(final ThreadPoolExecutor executor, final EJBMetaDataImpl ejb, final ServerMetaData server, final ClientMetaData client, final JNDIContext.AuthenticationInfo auth) {
        super(executor, ejb, server, client, auth);
    }

    @Override
    protected Object findX(final Method method, final Object[] args, final Object proxy) throws Throwable {
        throw new SystemException(new UnsupportedOperationException("Session beans may not have find methods"));
    }

    @Override
    protected Object removeByPrimaryKey(final Method method, final Object[] args, final Object proxy) throws Throwable {
        throw new ApplicationException(new RemoveException("Session objects are private resources and do not have primary keys"));
    }

    @Override
    protected Object removeWithHandle(final Method method, final Object[] args, final Object proxy) throws Throwable {
        // you can't really remove a stateless handle
        return null;
    }

    protected EJBObjectHandler newEJBObjectHandler() {
        return new StatelessEJBObjectHandler();
    }

}
