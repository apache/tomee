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
package org.apache.openejb.jpa.integration.eclipselink;

import org.apache.geronimo.transaction.manager.TransactionImpl;
import org.eclipse.persistence.platform.server.JMXEnabledPlatform;
import org.eclipse.persistence.platform.server.JMXServerPlatformBase;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.transaction.AbstractSynchronizationListener;
import org.eclipse.persistence.transaction.JTATransactionController;

import javax.management.MBeanServer;
import javax.transaction.Synchronization;
import javax.transaction.TransactionManager;

public class OpenEJBServerPlatform extends JMXServerPlatformBase implements JMXEnabledPlatform {
    public OpenEJBServerPlatform(final DatabaseSession newDatabaseSession) {
        super(newDatabaseSession);
        try {
            mBeanServer = MBeanServer.class.cast(
                OpenEJBServerPlatform.class.getClassLoader().loadClass("org.apache.openejb.monitoring.LocalMBeanServer")
                    .getMethod("get").invoke(null));
            this.prepareServerSpecificServicesMBean();
        } catch (final Exception e) {
            // no-op
        }
    }

    @Override
    public boolean isRuntimeServicesEnabledDefault() {
        return "true".equalsIgnoreCase(System.getProperty("openejb.eclipselink.jmx", "true"));
    }

    @Override
    public Class<?> getExternalTransactionControllerClass() {
        return OpenEJBJTATransactionController.class;
    }
    
    @Override
    public void prepareServerSpecificServicesMBean() {
        if (isRuntimeServicesEnabledDefault() && getDatabaseSession() != null && shouldRegisterRuntimeBean) {
            this.setRuntimeServicesMBean(new MBeanOpenEJBRuntimeServices(getDatabaseSession()));
        }
    }

    public static class OpenEJBJTATransactionController extends JTATransactionController {
        @Override
        protected TransactionManager acquireTransactionManager() throws Exception {
            return TransactionManager.class.cast(
                OpenEJBJTATransactionController.class.getClassLoader().loadClass("org.apache.openejb.OpenEJB")
                    .getDeclaredMethod("getTransactionManager").invoke(null));
        }

        @Override
        protected void registerSynchronization_impl(AbstractSynchronizationListener listener, Object txn) throws Exception {
            final TransactionImpl transaction = (TransactionImpl) txn;
            final Synchronization synchronization = (Synchronization) listener;
            transaction.registerInterposedSynchronization(synchronization);
        }
    }
}
