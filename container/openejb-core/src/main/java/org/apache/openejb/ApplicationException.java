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
 * This exception is thrown when a normal EnterpriseBean exception is thrown.
 * It acts as a carrier or wrapper for the actual application exception.
 *
 * The ApplicationException's nested exception will be either an EJB spec
 * defined ApplicationException ( or a custom exception defined by the bean
 * developer) or a RemoteException.
 *
 * The org.apache.openejb.ApplicationException must be caught and its nested
 * exception rethrown by the bean proxy to the client.
 *
 * The org.apache.openejb.ApplicationException is non-system exception; it does NOT
 * indicate a problem with the contaienr itself.
 *
 * @see ApplicationException
 * @see InvalidateReferenceException
 * @see OpenEJBException
 * @see SystemException
 */
public class ApplicationException extends OpenEJBException {

    public ApplicationException() {
        super();
    }

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(Exception e) {
        super(e);
    }

    public ApplicationException(Throwable t) {
        super(t);
    }

    public ApplicationException(String message, Exception e) {
        super(message, e);
    }
}