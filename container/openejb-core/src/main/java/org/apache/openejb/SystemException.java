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
 * This exception is thrown when the container has encountered an unresolvable
 * system exception that make this Container unable to process requests.
 * A breakdown in communication with one of the primary services or a
 * RuntimeException thrown within the container (not by a bean) is are good
 * examples.
 *
 * The org.apache.openejb.SystemException represents a serious problem with the
 * Container or request.
 *
 * NOTE: This exception bears no resemblence to the unchecked exceptions and
 * errors that an enterprise bean instance may throw during the
 * execution of a session or entity bean business method, a message-driven bean
 * onMessage method, or a container callback method (e.g. ejbLoad).
 * See InvalidateReferenceException for this.
 *
 * @see ApplicationException
 * @see InvalidateReferenceException
 * @see OpenEJBException
 * @see SystemException
 */
public class SystemException extends OpenEJBException {

    public SystemException() {
        super();
    }

    public SystemException(String message) {
        super(message);
    }

    public SystemException(Throwable rootCause) {
        super(rootCause);
    }

    public SystemException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

}
