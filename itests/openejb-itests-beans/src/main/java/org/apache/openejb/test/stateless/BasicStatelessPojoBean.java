/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.stateless;

import org.apache.openejb.test.AnnotatedApplicationException;
import org.apache.openejb.test.ApplicationException;
import org.apache.openejb.test.object.OperationsPolicy;

import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.annotation.Resource;
import java.util.Properties;
import java.rmi.RemoteException;

public class BasicStatelessPojoBean implements BasicStatelessBusinessLocal, BasicStatelessBusinessRemote {

    /**
     * Maps to BasicStatelessObject.businessMethod
     *
     * @return
     * @see org.apache.openejb.test.stateless.BasicStatelessObject#businessMethod
     */
    public String businessMethod(final String text) {
        final StringBuffer b = new StringBuffer(text);
        return b.reverse().toString();
    }

    public void scheduleTimer(final String name) {
        // ejbContext.getTimerService().createTimer(1, name);
    }

    public Object echo(final Object object) {
        return object;
    }

    /**
     * Throws an ApplicationException when invoked
     */
    public void throwApplicationException() throws ApplicationException {
        throw new ApplicationException("Testing ability to throw Application Exceptions");
    }

    /**
     * Throws an ApplicationException when invoked
     */
    public void throwAnnotatedApplicationException() {
        throw new AnnotatedApplicationException("Testing ability to throw Application Exceptions (annotated)");
    }

    /**
     * Throws a java.lang.NullPointerException when invoked
     * This is a system exception and should result in the
     * destruction of the instance and invalidation of the
     * remote reference.
     */
    public void throwSystemException_NullPointer() {
        throw new NullPointerException("Testing ability to throw System Exceptions");
    }

    /**
     * Maps to BasicStatelessObject.getPermissionsReport
     *
     * Returns a report of the bean's
     * runtime permissions
     *
     * @return
     * @see org.apache.openejb.test.stateless.BasicStatelessObject#getPermissionsReport
     */
    public Properties getPermissionsReport() {
        /* TO DO: */
        return null;
    }

    /**
     * Maps to BasicStatelessObject.getAllowedOperationsReport
     *
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     *
     * @param methodName The method for which to get the allowed opperations report
     * @return
     * @see org.apache.openejb.test.stateless.BasicStatelessObject#getAllowedOperationsReport
     */
    public OperationsPolicy getAllowedOperationsReport(final String methodName) {
        return null;
    }

    @Resource
    public void setSessionContext(final SessionContext ctx) throws EJBException, RemoteException {
    }

    public Object remove() {
        return "Executed remove() Method";
    }

    public String remove(final String arg) {
        return arg;
    }
}
