/*
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
package org.apache.openejb.arquillian.tests.bmp.local;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import jakarta.ejb.EntityBean;
import jakarta.ejb.EntityContext;

public class LittleFinderBean implements EntityBean {
    private static final long serialVersionUID = -1719910497840449494L;

    public void ejbActivate() throws RemoteException {
    }

    public void ejbPassivate() throws RemoteException {
    }

    public void setEntityContext(final EntityContext context) throws RemoteException {
    }

    public void unsetEntityContext() throws RemoteException {
    }

    public void ejbLoad() throws RemoteException {
    }

    public void ejbStore() throws RemoteException {
    }

    public void ejbRemove() throws RemoteException {
    }

    public Collection<LittleFinderPK> ejbFindAll() throws Exception {
        final ArrayList<LittleFinderPK> pks = new ArrayList();
        pks.add(new LittleFinderPK("1A"));
        return pks;
    }

}
