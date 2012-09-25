/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.ee.event;

import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.TransactionService;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class TransactionalEventNotifier
{
    private static final Logger logger = WebBeansLoggerFacade.getLogger(TransactionalEventNotifier.class);

    private TransactionalEventNotifier()
    {
        // utility class ct
    }

    /**
     * This will get called by the EJB integration code
     */
    public static void registerTransactionSynchronization(TransactionPhase phase, ObserverMethod<? super Object> observer, Object event) throws Exception
    {
        TransactionService transactionService = WebBeansContext.currentInstance().getService(TransactionService.class);
        
        Transaction transaction = null;
        if(transactionService != null)
        {
            transaction = transactionService.getTransaction();
        }
        
        if(transaction != null)
        {
            if (phase.equals(TransactionPhase.AFTER_COMPLETION))
            {
                transaction.registerSynchronization(new AfterCompletion(observer, event));
            }
            else if (phase.equals(TransactionPhase.AFTER_SUCCESS))
            {
                transaction.registerSynchronization(new AfterCompletionSuccess(observer, event));
            }
            else if (phase.equals(TransactionPhase.AFTER_FAILURE))
            {
                transaction.registerSynchronization(new AfterCompletionFailure(observer, event));
            }
            else if (phase.equals(TransactionPhase.BEFORE_COMPLETION))
            {
                transaction.registerSynchronization(new BeforeCompletion(observer, event));
            }
            else
            {
                throw new IllegalStateException(WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0007) + phase);
            }            
        }        
    }
    
    private static class AbstractSynchronization<T> implements Synchronization
    {

        private final ObserverMethod<T> observer;
        private final T event;

        public AbstractSynchronization(ObserverMethod<T> observer, T event)
        {
            this.observer = observer;
            this.event = event;
        }

        public void beforeCompletion()
        {
            // Do nothing
        }

        public void afterCompletion(int i)
        {
            //Do nothing
        }

        public void notifyObserver()
        {
            try
            {
                observer.notify(event);
            }
            catch (Exception e)
            {
                logger.log(Level.SEVERE, OWBLogConst.ERROR_0003, e);
            }
        }
    }

    private final static class BeforeCompletion extends AbstractSynchronization
    {
        private BeforeCompletion(ObserverMethod observer, Object event)
        {
            super(observer, event);
        }

        @Override
        public void beforeCompletion()
        {
            notifyObserver();
        }
    }

    private final static class AfterCompletion extends AbstractSynchronization
    {
        private AfterCompletion(ObserverMethod observer, Object event)
        {
            super(observer, event);
        }

        @Override
        public void afterCompletion(int i)
        {
            notifyObserver();
        }
    }

    private final static class AfterCompletionSuccess extends AbstractSynchronization
    {
        private AfterCompletionSuccess(ObserverMethod observer, Object event)
        {
            super(observer, event);
        }

        @Override
        public void afterCompletion(int i)
        {
            if (i == Status.STATUS_COMMITTED)
            {
                notifyObserver();
            }
        }
    }

    private final static class AfterCompletionFailure extends AbstractSynchronization
    {
        private AfterCompletionFailure(ObserverMethod observer, Object event)
        {
            super(observer, event);
        }

        @Override
        public void afterCompletion(int i)
        {
            if (i != Status.STATUS_COMMITTED)
            {
                notifyObserver();
            }
        }
    }
    
}
