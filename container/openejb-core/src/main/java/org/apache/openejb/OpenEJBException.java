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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb;

/**
 * The OpenEJBException is the standard exception thrown by all methods in all
 * type in the Container Provider Interface (CPI).  The OpenEJBException has 3
 * subtypes each serving a different purpose.  The Container will always thrown
 * one of these subtype and should never the OpenEJBException itself.
 * <ul>
 * <li><b>org.apache.openejb.ApplicationException</b><br>
 *
 *     This type is thrown when a normal EnterpriseBean exception is thrown.
 *     The ApplicationException's nested Exception will be either an EJB
 *     ApplicationException (a custom exception defined by the bean developer)
 *     or a RemoteException.  The org.openejb.ApplicationException must be
 *     caught and its nested exception rethrown by the bean proxy to the client.
 *     The org.apache.openejb.ApplicationException is non-system exception; it
 *     does NOT indicate a problem with the contaienr itself.
 *
 * <li><b>org.apache.openejb.InvalidateReferenceException</b><br>
 *
 *     This type is thrown when the EnterpriseBean throws a RuntimeException or
 *     system exception that results in the eviction of the bean instance.  The
 *     InvalidateReferenceException's nested exception will be a RemoteException
 *     or a RuntimeException, which must be converted to a RemoteException if
 *     applicable for the given beany proxy type.  The Application Server must
 *     catch the InvalidateReferenceException and its nested exception rethrown
 *     by the bean proxy (if the nested exception is a RuntimeException it must
 *     first be converted to a RemoteException if the bean proxy is a
 *     java.rmi.Remote proxy). After the exception is re-thrown by the bean
 *     proxy, the bean proxy must be invalidated so that all subsequent
 *     invocations by the client on that bean proxy throw a RemoteException. The
 *     proxy is made invalid. InvalidateReferenceException is non-system
 *     exception; it does NOT indicate a problem with the container itself.
 *
 * <li><b>org.apache.openejb.SystemException</b><br>
 *
 *     This type is thrown when the container has encountered an unresolvable
 *     system exception that make this Container unable to process requests.  A
 *     breakdown in communication with one of the primary services or a
 *     RuntimeException thrown within the container (not by a bean) is are good
 *     examples.  The org.openejb.SystemException represents a serious problem
 *     with the Container.  The Container should be shut down and not used for
 *     any more processing.
 *
 * </ul>
 * 
 * @see ApplicationException
 * @see InvalidateReferenceException
 * @see OpenEJBException
 * @see SystemException
 */
public class OpenEJBException extends Exception {

    public OpenEJBException() {
        super();
    }

    public OpenEJBException(String message) {
        super(message);
    }

    public OpenEJBException(Throwable rootCause) {
        super(rootCause);
    }

    public OpenEJBException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public String getMessage() {
        Throwable rootCause = getCause();
        if (rootCause != null) {
            return super.getMessage() + ": " + rootCause.getMessage();
        } else {
            return super.getMessage();
        }
    }

    public Throwable getRootCause() {
        return super.getCause();
    }

}
