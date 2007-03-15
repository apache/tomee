/**
 *
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
package org.apache.openejb.test.entity.cmp;

import org.apache.openejb.test.ApplicationException;
import org.apache.openejb.test.object.OperationsPolicy;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;
import java.util.Properties;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public interface UnknownCmpObject extends EJBObject {
    /**
     * Reverses the string passed in then returns it
     */
    String businessMethod(String text) throws RemoteException;

    /**
     * Throws an ApplicationException when invoked
     */
    void throwApplicationException() throws RemoteException, ApplicationException;

    /**
     * Throws a java.lang.NullPointerException when invoked
     * This is a system exception and should result in the
     * destruction of the instance and invalidation of the
     * remote reference.
     */
    void throwSystemException_NullPointer() throws RemoteException;

    /**
     * Returns a report of the bean's
     * runtime permissions
     */
    Properties getPermissionsReport() throws RemoteException;

    /**
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     *
     * @param methodName The method for which to get the allowed opperations report
     */
    OperationsPolicy getAllowedOperationsReport(String methodName) throws RemoteException;
}
