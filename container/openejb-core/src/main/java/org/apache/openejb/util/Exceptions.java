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

package org.apache.openejb.util;

import org.apache.openejb.OpenEjbContainer.NoModulesFoundException;

import jakarta.ejb.EJBException;
import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import jakarta.transaction.RollbackException;
import java.io.IOException;
import java.io.NotSerializableException;

/**
 * @version $Rev$ $Date$
 */
public class Exceptions {

    /**
     * Removes the need for a cast when using initCause
     *
     * @param t
     * @param cause
     * @return
     */
    public static <T extends Throwable> T initCause(final T t, final Throwable cause) {
        return (T) t.initCause(cause);
    }


    public static IOException newIOException(final String message, final Throwable cause) {
        return initCause(new IOException(message), cause);
    }

    public static IOException newIOException(final Throwable cause) {
        return initCause(new IOException(), cause);
    }

    public static NamingException newNamingException(final String message, final Throwable cause) {
        return initCause(new NamingException(message), cause);
    }

    public static NamingException newNamingException(final Throwable cause) {
        return initCause(new NamingException(), cause);
    }


    public static RollbackException newRollbackException(final String message, final Throwable cause) {
        return initCause(new RollbackException(message), cause);
    }

    public static RollbackException newRollbackException(final Throwable cause) {
        return initCause(new RollbackException(), cause);
    }


    public static AuthenticationException newAuthenticationException(final String message, final Throwable cause) {
        return initCause(new AuthenticationException(message), cause);
    }

    public static AuthenticationException newAuthenticationException(final Throwable cause) {
        return initCause(new AuthenticationException(), cause);
    }


    public static EJBException newEJBException(final String message, final Throwable cause) {
        return initCause(new EJBException(message), cause);
    }

    public static EJBException newEJBException(final Throwable cause) {
        return initCause(new EJBException(), cause);
    }


    public static NotSerializableException newNotSerializableException(final String message, final Throwable cause) {
        return initCause(new NotSerializableException(message), cause);
    }

    public static NotSerializableException newNotSerializableException(final Throwable cause) {
        return initCause(new NotSerializableException(), cause);
    }

    public static NoModulesFoundException newNoModulesFoundException() {
        return new NoModulesFoundException(Join.join("\n", "No modules found to deploy.", "1)Maybe descriptors are placed in incorrect location.", "Descriptors could go under: ", "<base-dir>/META-INF or <base-dir>/WEB-INF", "but not directly under <base-dir>", "Check 'Application Discovery via the Classpath' docs page for more info", "2)Maybe no modules are present in the classpath.", "Is 'openejb.base' system property pointing to the intended location?")
        );
    }


}
