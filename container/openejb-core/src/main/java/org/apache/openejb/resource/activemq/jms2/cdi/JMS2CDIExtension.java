/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.resource.activemq.jms2.cdi;

import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.activemq.jms2.JMS2;
import org.apache.openejb.spi.ContainerSystem;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.jms.BytesMessage;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.ConnectionMetaData;
import jakarta.jms.Destination;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSPasswordCredential;
import jakarta.jms.JMSProducer;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.JMSSessionMode;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.StreamMessage;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TemporaryTopic;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import javax.naming.NamingException;
import jakarta.transaction.TransactionScoped;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

// this extension adds to CDI a producer a JMSContext,
// the producer creates the JMSContext only if not already there
// and it switch to a facade request scoped or transaction scoped depending if there is a tx we can reuse or not
public class JMS2CDIExtension implements Extension {
    @ApplicationScoped
    public static class ContextProducer { // small hack to reuse CDI proxying and get it properly setup when injected
        @Inject
        private RequestAutoContextDestruction requestScoped;

        @Inject
        private TransactionAutoContextDestruction transactionScoped;

        @Produces
        public JMSContext context(final InjectionPoint ip) {
            return new InternalJMSContext(newKey(ip), requestScoped, transactionScoped);
        }

        private Key newKey(final InjectionPoint ip) {
            final Annotated annotated = ip.getAnnotated();
            final JMSConnectionFactory jmsConnectionFactory = annotated.getAnnotation(JMSConnectionFactory.class);
            final JMSSessionMode sessionMode = annotated.getAnnotation(JMSSessionMode.class);
            final JMSPasswordCredential credential = annotated.getAnnotation(JMSPasswordCredential.class);

            final String jndi = "openejb:Resource/" +
                (jmsConnectionFactory == null ? findAnyConnectionFactory() : findMatchingConnectionFactory(jmsConnectionFactory.value()));
            return new Key(
                jndi,
                credential != null ? credential.userName() : null,
                credential != null ? credential.password() : null,
                sessionMode != null ? sessionMode.value() : null);
        }

        private String findMatchingConnectionFactory(final String value) {
            final OpenEjbConfiguration component = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
            if (component != null && component.facilities != null) {
                for (final ResourceInfo ri : component.facilities.resources) {
                    if (!ri.types.contains("jakarta.jms.ConnectionFactory")) {
                        continue;
                    }
                    if (ri.id.equals(value)) {
                        return ri.id;
                    }
                }
                // try application ones
                for (final ResourceInfo ri : component.facilities.resources) {
                    if (!ri.types.contains("jakarta.jms.ConnectionFactory")) {
                        continue;
                    }
                    if (ri.id.endsWith(value)) {
                        return ri.id;
                    }
                }
            }
            // something is wrong, just fail
            throw new IllegalArgumentException(
                    "No connection factory found, either use @JMSConnectionFactory JMSContext or define a connection factory");
        }

        private String findAnyConnectionFactory() {
            final OpenEjbConfiguration component = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
            if (component != null && component.facilities != null) {
                for (final ResourceInfo ri : component.facilities.resources) {
                    if (ri.types.contains("jakarta.jms.ConnectionFactory")) {
                        return ri.id;
                    }
                }

                // try the default one
                return "DefaultJMSConnectionFactory";
            }
            // something is wrong, just fail
            throw new IllegalArgumentException(
                "No connection factory found, either use @JMSConnectionFactory JMSContext or define a connection factory");
        }
    }

    public abstract static class AutoContextDestruction implements Serializable {
          private static final long serialVersionUID = 1L;
        private transient Map<Key, JMSContext> contexts = new ConcurrentHashMap<>();

        public void push(final Key key, final JMSContext c) {
            contexts.put(key, c);
        }

        public JMSContext find(final Key key) {
            return contexts.get(key);
        }

        @PreDestroy
        private void destroy() {
            if (contexts != null) {
                JMSRuntimeException jre = null;
                for (final JMSContext c : contexts.values()) {
                    try {
                        c.close();
                    } catch (final JMSRuntimeException e) {
                        jre = e;
                    }
                }
                if (jre != null) {
                    throw jre;
                }
            }
        }
    }

    @RequestScoped
    public static class RequestAutoContextDestruction extends AutoContextDestruction {
        private static final long serialVersionUID = 1L;
    }

    @TransactionScoped
    public static class TransactionAutoContextDestruction extends AutoContextDestruction {
        private static final long serialVersionUID = 1L;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;
        private volatile ConnectionFactory connectionFactoryInstance;
        private final String connectionFactory;
        private final String username;
        private final String password;
        private final Integer session;
        private final int hash;

        public Key(final String connectionFactory, final String username, final String password, final Integer session) {
            this.connectionFactory = connectionFactory;
            this.username = username;
            this.password = password;
            this.session = session;

            int result = connectionFactory != null ? connectionFactory.hashCode() : 0;
            result = 31 * result + (username != null ? username.hashCode() : 0);
            result = 31 * result + (password != null ? password.hashCode() : 0);
            result = 31 * result + (session != null ? session.hashCode() : 0);
            this.hash = result;
        }

        private ConnectionFactory connectionFactory() {
            if (connectionFactoryInstance != null) {
                return connectionFactoryInstance;
            }
            synchronized (this) {
                if (connectionFactoryInstance != null) {
                    return connectionFactoryInstance;
                }
                try {
                    return connectionFactoryInstance = ConnectionFactory.class.cast(
                        SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext()
                            .lookup(connectionFactory));
                } catch (final NamingException e) {
                    throw new JMSRuntimeException(e.getMessage(), null, e);
                }
            }
        }

        public JMSContext create() {
            if (username != null && session != null) {
                return connectionFactory().createContext(username, password, session);
            } else if (username != null) {
                return connectionFactory().createContext(username, password);
            } else if (session != null) {
                return connectionFactory().createContext(session);
            }
            return connectionFactory().createContext();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Key key = Key.class.cast(o);
            return connectionFactory != null ? connectionFactory.equals(key.connectionFactory) : key.connectionFactory == null
                && (username != null ? username.equals(key.username) : key.username == null
                && (password != null ? password.equals(key.password) : key.password == null
                && (Objects.equals(session, key.session))));

        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    public static class InternalJMSContext implements JMSContext, Serializable {
        private static final long serialVersionUID = 1L;
        private final Key key;
        private final RequestAutoContextDestruction requestStorage;
        private final TransactionAutoContextDestruction transactionStorage;

        public InternalJMSContext(final Key key, final RequestAutoContextDestruction requestScoped, final TransactionAutoContextDestruction transactionScoped) {
            this.key = key;
            this.requestStorage = requestScoped;
            this.transactionStorage = transactionScoped;
        }

        private synchronized JMSContext context() {
            if (JMS2.inTx()) {
                return findOrCreateContext(transactionStorage);
            }
            return findOrCreateContext(requestStorage);
        }

        private JMSContext findOrCreateContext(final AutoContextDestruction storage) {
            JMSContext jmsContext = storage.find(key);
            if (jmsContext == null) { // both scopes are thread safe
                jmsContext = key.create();
                storage.push(key, jmsContext);
            }
            return jmsContext;
        }

        // plain delegation now

        @Override
        public void acknowledge() {
            context().acknowledge();
        }

        @Override
        public void close() {
            context().close();
        }

        @Override
        public void commit() {
            context().commit();
        }

        @Override
        public QueueBrowser createBrowser(final Queue queue) {
            return context().createBrowser(queue);
        }

        @Override
        public QueueBrowser createBrowser(final Queue queue, final String messageSelector) {
            return context().createBrowser(queue, messageSelector);
        }

        @Override
        public BytesMessage createBytesMessage() {
            return context().createBytesMessage();
        }

        @Override
        public JMSConsumer createConsumer(final Destination destination) {
            return context().createConsumer(destination);
        }

        @Override
        public JMSConsumer createConsumer(final Destination destination, final String messageSelector) {
            return context().createConsumer(destination, messageSelector);
        }

        @Override
        public JMSConsumer createConsumer(final Destination destination, final String messageSelector, final boolean noLocal) {
            return context().createConsumer(destination, messageSelector, noLocal);
        }

        @Override
        public JMSContext createContext(final int sessionMode) {
            return context().createContext(sessionMode);
        }

        @Override
        public JMSConsumer createDurableConsumer(final Topic topic, final String name) {
            return context().createDurableConsumer(topic, name);
        }

        @Override
        public JMSConsumer createDurableConsumer(final Topic topic, final String name, final String messageSelector, final boolean noLocal) {
            return context().createDurableConsumer(topic, name, messageSelector, noLocal);
        }

        @Override
        public MapMessage createMapMessage() {
            return context().createMapMessage();
        }

        @Override
        public Message createMessage() {
            return context().createMessage();
        }

        @Override
        public ObjectMessage createObjectMessage() {
            return context().createObjectMessage();
        }

        @Override
        public ObjectMessage createObjectMessage(final Serializable object) {
            return context().createObjectMessage(object);
        }

        @Override
        public JMSProducer createProducer() {
            return context().createProducer();
        }

        @Override
        public Queue createQueue(final String queueName) {
            return context().createQueue(queueName);
        }

        @Override
        public JMSConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName) {
            return context().createSharedConsumer(topic, sharedSubscriptionName);
        }

        @Override
        public JMSConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName, final String messageSelector) {
            return context().createSharedConsumer(topic, sharedSubscriptionName, messageSelector);
        }

        @Override
        public JMSConsumer createSharedDurableConsumer(final Topic topic, final String name) {
            return context().createSharedDurableConsumer(topic, name);
        }

        @Override
        public JMSConsumer createSharedDurableConsumer(final Topic topic, final String name, final String messageSelector) {
            return context().createSharedDurableConsumer(topic, name, messageSelector);
        }

        @Override
        public StreamMessage createStreamMessage() {
            return context().createStreamMessage();
        }

        @Override
        public TemporaryQueue createTemporaryQueue() {
            return context().createTemporaryQueue();
        }

        @Override
        public TemporaryTopic createTemporaryTopic() {
            return context().createTemporaryTopic();
        }

        @Override
        public TextMessage createTextMessage() {
            return context().createTextMessage();
        }

        @Override
        public TextMessage createTextMessage(final String text) {
            return context().createTextMessage(text);
        }

        @Override
        public Topic createTopic(final String topicName) {
            return context().createTopic(topicName);
        }

        @Override
        public boolean getAutoStart() {
            return context().getAutoStart();
        }

        @Override
        public String getClientID() {
            return context().getClientID();
        }

        @Override
        public ExceptionListener getExceptionListener() {
            return context().getExceptionListener();
        }

        @Override
        public ConnectionMetaData getMetaData() {
            return context().getMetaData();
        }

        @Override
        public int getSessionMode() {
            return context().getSessionMode();
        }

        @Override
        public boolean getTransacted() {
            return context().getTransacted();
        }

        @Override
        public void recover() {
            context().recover();
        }

        @Override
        public void rollback() {
            context().rollback();
        }

        @Override
        public void setAutoStart(final boolean autoStart) {
            context().setAutoStart(autoStart);
        }

        @Override
        public void setClientID(final String clientID) {
            context().setClientID(clientID);
        }

        @Override
        public void setExceptionListener(final ExceptionListener listener) {
            context().setExceptionListener(listener);
        }

        @Override
        public void start() {
            context().start();
        }

        @Override
        public void stop() {
            context().stop();
        }

        @Override
        public void unsubscribe(final String name) {
            context().unsubscribe(name);
        }
    }

    public void addContextProducer(@Observes final BeforeBeanDiscovery beforeBeanDiscovery, final BeanManager beanManager) {
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(ContextProducer.class));
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(RequestAutoContextDestruction.class));
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TransactionAutoContextDestruction.class));
    }
}
