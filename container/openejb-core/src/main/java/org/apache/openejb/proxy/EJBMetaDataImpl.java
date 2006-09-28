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
package org.apache.openejb.proxy;

import java.io.ObjectStreamException;
import java.io.Serializable;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class EJBMetaDataImpl implements EJBMetaData, Serializable {

    private final EJBHomeImpl ejbHome;
    private final Class homeInterfaceClass;
    private final Class remoteInterfaceClass;
    private final Class primaryKeyClass;
    private final boolean session;
    private final boolean statelessSession;

    public EJBMetaDataImpl(EJBHomeImpl ejbHome, Class homeInterfaceClass, Class remoteInterfaceClass, Class primaryKeyClass, boolean session, boolean statelessSession) {
        this.ejbHome = ejbHome;
        this.homeInterfaceClass = homeInterfaceClass;
        this.remoteInterfaceClass = remoteInterfaceClass;
        this.primaryKeyClass = primaryKeyClass;
        this.session = session;
        this.statelessSession = statelessSession;
    }

    public EJBHome getEJBHome() {
        return ejbHome;
    }

    public Class getHomeInterfaceClass() {
        return homeInterfaceClass;
    }

    public Class getRemoteInterfaceClass() {
        return remoteInterfaceClass;
    }

    public Class getPrimaryKeyClass() {
        if (session) {
            throw new EJBException("Cannot use getPrimaryKey() on a session bean");
        }
        return primaryKeyClass;
    }

    public boolean isSession() {
        return session;
    }

    public boolean isStatelessSession() {
        return statelessSession;
    }
    
    protected Object writeReplace() throws ObjectStreamException{
        return SerializationHandler.writeReplace(this, this.ejbHome.getProxyInfo());
    }
}
