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
package org.apache.openejb.deployment.entity.cmp.ejbql;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

import org.apache.openejb.deployment.entity.cmp.cmr.CompoundPK;


/**
 *
 * @version $Revision$ $Date$
 */
public interface AHome extends EJBHome {

    // Create
    public ARemote create(Integer field1) throws CreateException, RemoteException;
    public ARemote create(CompoundPK primaryKey) throws CreateException, RemoteException;

    // Finder
    public ARemote findByPrimaryKey(Integer primaryKey) throws FinderException, RemoteException;
    public ARemote findTest(String value) throws FinderException, RemoteException;
    
}
