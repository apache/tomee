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
package org.apache.openejb.webadmin;

import java.rmi.RemoteException;

import javax.ejb.CreateException;

/** This is the standard EJB Home interface for the webadmin.  It contains the
 * create method.
 * @author <a href="mailto:tim_urberg@yahoo.com">Tim Uberg</a>
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public interface HttpHome extends javax.ejb.EJBHome {
    /** Creates a new EJB object for the web administration
     * @throws RemoteException if an exception is thrown
     * @throws CreateException if an exception is thrown
     * @return The HttpObject for this bean
     */    
    public HttpObject create() throws RemoteException, CreateException;
}
