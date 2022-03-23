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


package org.apache.openejb.cdi;

import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.ee.event.TransactionalEventNotifier;
import org.apache.webbeans.event.EventMetadataImpl;
import org.apache.webbeans.spi.TransactionService;

import jakarta.enterprise.event.TransactionPhase;
import jakarta.enterprise.inject.spi.ObserverMethod;
import javax.naming.NamingException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/**
 * this is a copy of the class in the owb webbeans-openejb jar which we aren't using.
 */
public class OpenEJBTransactionService implements TransactionService {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_CDI, OpenEJBTransactionService.class);

    private final ContainerSystem containerSystem;
    private final WebBeansContext webBeansContext;

    public OpenEJBTransactionService(final WebBeansContext webBeansContext) {
        this.containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        this.webBeansContext = webBeansContext;
    }

    @Override
    public Transaction getTransaction() {
        final TransactionManager manager = getTransactionManager();
        if (manager != null) {
            try {
                return manager.getTransaction();
            } catch (final SystemException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public TransactionManager getTransactionManager() {
        return SystemInstance.get().getComponent(TransactionManager.class);
    }

    @Override
    public UserTransaction getUserTransaction() {
        UserTransaction ut;
        try {
            ut = (UserTransaction) containerSystem.getJNDIContext().lookup("comp/UserTransaction");
        } catch (final NamingException e) {
            logger.debug("User transaction is not bound to context, lets create it");
            ut = new CoreUserTransaction(getTransactionManager());

        }
        return ut;
    }

    @Override
    public void registerTransactionSynchronization(final TransactionPhase phase, final ObserverMethod<? super Object> observer, final Object event) throws Exception {
        Set<Annotation> qualifiers = observer.getObservedQualifiers();
        if (qualifiers == null) {
            qualifiers = Collections.emptySet();
        }

        TransactionalEventNotifier.registerTransactionSynchronization(phase, observer, event,
            new EventMetadataImpl(observer.getObservedType(), null, null,
                qualifiers.toArray(new Annotation[qualifiers.size()]), webBeansContext));
    }
}
