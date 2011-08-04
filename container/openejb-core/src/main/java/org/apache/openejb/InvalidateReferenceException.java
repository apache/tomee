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
 * This type is thrown when the EnterpriseBean throws a RuntimeException or
 * system exception that results in the eviction of the bean instance.  The
 * InvalidateReferenceException's nested exception will be a RemoteException
 * or possibly an ObjectNotFoundException.
 *
 * The Application Server must catch the InvalidateReferenceException and its
 * nested exception rethrown by the bean proxy. After the exception is
 * re-thrown by the bean proxy, the bean proxy must be invalidated so that all
 * subsequent invocations by the client on that bean proxy throw a
 * RemoteException. The proxy is made invalid. InvalidateReferenceException is
 * non-system exception; it does NOT indicate a problem with the container
 * itself.
 *
 * @see ApplicationException
 * @see InvalidateReferenceException
 * @see OpenEJBException
 * @see SystemException
 */
public class InvalidateReferenceException extends ApplicationException {

    public InvalidateReferenceException() {
        super();
    }

    public InvalidateReferenceException(String message) {
        super(message);
    }

    public InvalidateReferenceException(Exception e) {
        super(e);
    }

    public InvalidateReferenceException(Throwable t) {
        super(t);
    }

    public InvalidateReferenceException(String message, Exception e) {
        super(message, e);
    }

}
