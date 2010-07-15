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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb;

import org.apache.openejb.client.LocalInitialContext;
import org.apache.openejb.client.LocalInitialContextFactory;

import javax.ejb.embeddable.EJBContainer;
import javax.ejb.spi.EJBContainerProvider;
import javax.naming.Context;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class OpenEjbContainer extends EJBContainer {

    private final Context context;

    public OpenEjbContainer(Context context) {
        this.context = context;
    }

    @Override
    public void close() {
        try {
            context.close();
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Context getContext() {
        return context;
    }


    public static class Provider implements EJBContainerProvider {

        @Override
        public EJBContainer createEJBContainer(Map<?, ?> properties) {
            try {
                final Hashtable hashtable = new Hashtable(properties);
                hashtable.put(LocalInitialContext.ON_CLOSE, LocalInitialContext.Close.DESTROY.name());
                final Context context = new LocalInitialContextFactory().getInitialContext(hashtable);
                return new OpenEjbContainer(context);
            } catch (NamingException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
