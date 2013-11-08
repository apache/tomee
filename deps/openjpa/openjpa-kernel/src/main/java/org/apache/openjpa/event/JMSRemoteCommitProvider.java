/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.event;

import java.util.Properties;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.GenericConfigurable;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.UserException;

/**
 * JMS-based implementation of {@link RemoteCommitProvider} that
 * listens for object modifications and propagates those changes to
 * other RemoteCommitProviders over a JMS topic.
 *
 * @author Patrick Linskey
 * @since 0.2.5.0
 */
public class JMSRemoteCommitProvider
    extends AbstractRemoteCommitProvider
    implements Configurable, GenericConfigurable, ExceptionListener {

    private static Localizer s_loc = Localizer.forPackage
        (JMSRemoteCommitProvider.class);

    private String _topicName = "topic/OpenJPACommitProviderTopic";
    private String _tcfName = "java:/ConnectionFactory";
    private Properties _ctxProps = null;
    private int _reconnectAttempts = 0;
    private TopicConnection _connection;
    private TopicSession _session;
    private TopicPublisher _publisher;
    private ClassLoader _appClassLoader;
    
    /**
     * Sets the JMS Topic name. Defaults to
     * <code>topic/OpenJPACommitProviderTopic</code>.
     */
    public void setTopic(String name) {
        _topicName = name;
    }

    /**
     * Sets the JMS TopicConnectionFactory name. Defaults to
     * <code>java:/ConnectionFactory</code>.
     */
    public void setTopicConnectionFactory(String name) {
        _tcfName = name;
    }

    /**
     * The number of times to attempt to reconnect after a JMS send exception
     * is detected. Defaults to 0, meaning no attempt to reconnect is made;
     * the exception is logged and ignored.
     */
    public void setExceptionReconnectAttempts(int attempts) {
        _reconnectAttempts = attempts;
    }

    /**
     * Set a map of properties to pass to the {@link InitialContext}
     * constructor for JNDI lookups. Implementation of
     * {@link GenericConfigurable}.
     */
    public void setInto(Options opts) {
        if (opts != null && !opts.isEmpty()) {
            _ctxProps = new Properties();
            _ctxProps.putAll(opts);
            opts.clear();
        } else
            _ctxProps = null;
    }

    /**
     * Returns a new {@link Context} object for use by this provider.
     */
    protected Context newContext()
        throws NamingException {
        if (_ctxProps == null)
            return new InitialContext();
        return new InitialContext(_ctxProps);
    }

    // ---------- RemoteCommitProvider implementation ----------

    public void broadcast(RemoteCommitEvent event) {
        try {
            _publisher.publish(createMessage(event));
            if (log.isTraceEnabled())
                log.trace(s_loc.get("jms-sent-update", _topicName));
        } catch (JMSException jmse) {
            if (log.isWarnEnabled())
                log.warn(s_loc.get("jms-send-error", _topicName), jmse);
        }
    }

    public void close() {
        try {
            if (_connection != null) {
                _connection.close();
                if (log.isInfoEnabled())
                    log.info(s_loc.get("jms-close-listener", _topicName));
            }
        }
        catch (JMSException jmse) {
            if (log.isWarnEnabled())
                log.warn(s_loc.get("jms-close-error", _topicName), jmse);
        } finally {
            _connection = null;
        }
    }

    // ---------- Configurable implementation ----------

    /**
     * Subclasses that need to perform actions in
     * {@link Configurable#endConfiguration} must invoke this method.
     */
    public void endConfiguration() {
        super.endConfiguration();
        _appClassLoader = Thread.currentThread().getContextClassLoader();
        connect();
    }

    protected void connect() {
        try {
            Context ctx = newContext();
            TopicConnectionFactory tcf =
                (TopicConnectionFactory) ctx.lookup(_tcfName);
            Topic topic = (Topic) ctx.lookup(_topicName);
            ctx.close();

            _connection = tcf.createTopicConnection();

            // false == not transacted.
            _session = _connection.createTopicSession
                (false, Session.AUTO_ACKNOWLEDGE);

            // create a publisher
            _publisher = _session.createPublisher(topic);

            // create a subscriber.
            TopicSubscriber s = _session.createSubscriber(topic, null,
                /* noLocal: */ true);
            
            MessageListener l = getMessageListener();
            s.setMessageListener(l);
            _connection.start();
            _connection.setExceptionListener(this);
            if (log.isInfoEnabled())
                log.info(s_loc.get("jms-start-listener", _topicName));
        } catch (OpenJPAException ke) {
            throw ke;
        } catch (Exception e) {
            throw new UserException(s_loc.get("jms-provider-config",
                _topicName, _tcfName), e).setFatal(true);
        }
    }

    /* *
     * Returns a {@link javax.jms.MessageListener} capable of
     * understanding and processing messages created by {@link #createMessage}.
     *  The listener returned by this method is responsible for
     * notifying the provider that a remote event has been received.
     */
    protected MessageListener getMessageListener() {
        return new MessageListener() {
            public void onMessage(Message m) {
                if (!(m instanceof ObjectMessage)) {
                    if (log.isWarnEnabled())
                        log.warn(s_loc.get("jms-receive-error-3",
                            _topicName, m.getClass().getName()));
                    return;
                }
                
                ClassLoader saveCls = Thread.currentThread()
                    .getContextClassLoader();
                try {
                    if (saveCls != _appClassLoader)
                        Thread.currentThread().setContextClassLoader(
                            _appClassLoader);
                    ObjectMessage om = (ObjectMessage) m;
                    Object o = om.getObject();

                    if (o instanceof RemoteCommitEvent) {
                    	if (log.isTraceEnabled())
                    		log.trace(s_loc.get("jms-received-update",
                    				_topicName));

                    	RemoteCommitEvent rce = (RemoteCommitEvent) o;
                    	fireEvent(rce);
                    } else {
                    	if (log.isWarnEnabled())
                    		log.warn(s_loc.get("jms-receive-error-2",
                                    o.getClass().getName(), _topicName));
                    }
                } catch (JMSException jmse) {
                    if (log.isWarnEnabled())
                        log.warn(s_loc.get("jms-receive-error-1"), jmse);
                } finally {
                    if (saveCls != _appClassLoader)
                        Thread.currentThread().setContextClassLoader(saveCls);
                }

            }
        };
    }

    /**
     * Returns a new {@link Message} to send to the topic. This
     * implementation creates an {@link ObjectMessage}.
     */
    protected Message createMessage(RemoteCommitEvent event)
        throws JMSException {
        return _session.createObjectMessage(event);
    }

    public void onException(JMSException ex) {
        if (log.isWarnEnabled())
            log.warn(s_loc.get("jms-listener-error", _topicName), ex);
        if (_reconnectAttempts <= 0)
            return;

        close();
        boolean connected = false;
        for (int i = 0; !connected && i < _reconnectAttempts; i++) {
            try {
                if (log.isInfoEnabled())
                    log.info(s_loc.get("jms-reconnect-attempt", _topicName,
                        String.valueOf(i + 1)));
                connect();
                connected = true;
            } catch (Exception e) {
                if (log.isInfoEnabled())
                    log.info(s_loc.get("jms-reconnect-fail", _topicName), e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }

        if (!connected && log.isErrorEnabled())
            log.error(s_loc.get("jms-cant-reconnect", _topicName,
                String.valueOf(_reconnectAttempts)));
        else if (connected && log.isInfoEnabled())
            log.info(s_loc.get("jms-reconnected", _topicName));
	}
}
