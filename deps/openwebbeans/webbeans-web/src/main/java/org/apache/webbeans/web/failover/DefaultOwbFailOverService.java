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
package org.apache.webbeans.web.failover;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.util.proxy.ProxyObjectOutputStream;

import javax.enterprise.context.Conversation;
import javax.enterprise.inject.spi.Bean;
import javax.servlet.http.HttpSession;

import org.apache.webbeans.config.OpenWebBeansConfiguration;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ConversationContext;
import org.apache.webbeans.context.SessionContext;
import org.apache.webbeans.conversation.ConversationManager;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.proxy.javassist.OpenWebBeansClassLoaderProvider;
import org.apache.webbeans.spi.FailOverService;
import org.apache.webbeans.web.context.SessionContextManager;
import org.apache.webbeans.web.context.WebContextsService;

/**
 * Default implementation of the {@link FailOverService}.
 */
public class DefaultOwbFailOverService implements FailOverService
{
    private static final Logger LOGGER = WebBeansLoggerFacade.getLogger(DefaultOwbFailOverService.class);

    public static final String CONFIG_IS_SUPPORT_FAILOVER = "org.apache.webbeans.web.failover.issupportfailover";
    public static final String CONFIG_IS_SUPPORT_PASSIVATE = "org.apache.webbeans.web.failover.issupportpassivation";
    public static final String CONFIG_RESOURCES_SERIALIZATION_HANDLER = "org.apache.webbeans.web.failover.resources.serialization.handler.v10";

    public static final String ATTRIBUTE_SESSION_CONTEXT = "sessionContext";
    public static final String ATTRIBUTE_CONVERSATION_CONTEXT_MAP = "conversatzionContextMap";

    private static final String JVM_ID = UUID.randomUUID().toString() + "_" + System.currentTimeMillis();

    private final WebBeansContext webBeansContext = WebBeansContext.currentInstance();

    private ThreadLocal<Boolean> passivation = new ThreadLocal<Boolean>();
    private boolean supportFailOver;
    private boolean supportPassivation;
    private SerializationHandlerV10 handler;

    public DefaultOwbFailOverService()
    {
        OpenWebBeansConfiguration config = webBeansContext.getOpenWebBeansConfiguration();

        String value;

        value = config.getProperty(CONFIG_IS_SUPPORT_FAILOVER);
        if (value != null && value.equalsIgnoreCase("true"))
        {
            supportFailOver = true;
        }

        value = config.getProperty(CONFIG_IS_SUPPORT_PASSIVATE);
        if (value != null && value.equalsIgnoreCase("true"))
        {
            supportPassivation = true;
        }

        if (supportFailOver || supportPassivation)
        {
            OpenWebBeansClassLoaderProvider.initProxyFactoryClassLoaderProvider();
            value = config.getProperty(CONFIG_RESOURCES_SERIALIZATION_HANDLER);

            if (value != null)
            {
                try
                {
                    handler = (SerializationHandlerV10) Class.forName(value).newInstance();
                }
                catch (Exception e)
                {
                    LOGGER.log(Level.SEVERE, "DefaultOwbFailOverService could not instanciate: [" + value + "]", e);
                }
            }
        }

        if (LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.log(Level.FINE, "IsSupportFailOver: [{0}]", String.valueOf(supportFailOver));
            LOGGER.log(Level.FINE, "IsSupportPassivation: [{0}]", String.valueOf(supportPassivation));
        }
    }

    public void sessionIsIdle(HttpSession session)
    {
        if (session != null)
        {
            FailOverBag bag = (FailOverBag) session.getAttribute(FailOverBag.SESSION_ATTRIBUTE_NAME);

            if (bag == null)
            {
                bag = new FailOverBag(session.getId(), getJvmId());
            }

            bag.setSessionInUse(false);

            storeBeansInFailOverBag(bag, session);

            addFailOverBagToSession(bag, session);
            addActivationListenerToSession(session);
        }

        passivation.remove();
        passivation.set(null);
    }

    public void sessionIsInUse(HttpSession session)
    {
        if (session != null)
        {
            FailOverBag bag = (FailOverBag) session.getAttribute(FailOverBag.SESSION_ATTRIBUTE_NAME);

            if (bag != null)
            {
                bag.setSessionInUse(true);
            }

            addActivationListenerToSession(session);
        }
    }

    public void sessionDidActivate(HttpSession session)
    {
        FailOverBag bag = (FailOverBag) session.getAttribute(FailOverBag.SESSION_ATTRIBUTE_NAME);

        if (bag != null)
        {
            if (bag.isSessionInUse())
            {
                if (LOGGER.isLoggable(Level.FINE))
                {
                    LOGGER.log(Level.FINE, "Skip restore beans for session [" + bag.getSessionId() + "] because session is in use.");
                }
            }
            else
            {
                if (LOGGER.isLoggable(Level.FINE))
                {
                    LOGGER.log(Level.FINE, "Restore beans for session [{0}]", session.getId());
                }

                restoreBeansFromFailOverBag(bag, session);
            }
        }
    }

    public void sessionWillPassivate(HttpSession session)
    {
        sessionIsIdle(session);

        passivation.set(true);
    }

    /**
     * Adds the {@link FailOverSessionActivationListener} to the current {@link HttpSession}.
     * It must not be manually registered when we store it as session attribute.
     * 
     * @param session The current {@link HttpSession}.
     */
    protected void addActivationListenerToSession(HttpSession session)
    {
        if (session.getAttribute(FailOverSessionActivationListener.SESSION_ATTRIBUTE_NAME) == null)
        {
            session.setAttribute(FailOverSessionActivationListener.SESSION_ATTRIBUTE_NAME, new FailOverSessionActivationListener());
        }
    }

    /**
     * Store the {@link FailOverBag} as attribute to the current {@link HttpSession}.
     * So when the session is fail over to other JVM or local disk, the
     * attribute could also be serialized.
     * 
     * @param bag The {@link FailOverBag}.
     * @param session The current {@link HttpSession}.
     */
    protected void addFailOverBagToSession(FailOverBag bag, HttpSession session)
    {
        try
        {
            session.setAttribute(FailOverBag.SESSION_ATTRIBUTE_NAME, bag);

            if (LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.log(Level.FINE, "Successfully added FailOverBag to session [" + bag.getSessionId() + "].");
            }
        }
        catch (Exception e)
        {
            String message = "Could not add FailOverBag to session [" + bag.getSessionId() + "].";
            LOGGER.log(Level.SEVERE, message, e);

            throw new WebBeansException(message, e);
        }
    }

    /**
     * Stores the session and conversation contexts in the {@link FailOverBag}.
     * 
     * @param bag The {@link FailOverBag}.
     * @param session The current {@link HttpSession}.
     */
    protected void storeBeansInFailOverBag(FailOverBag bag, HttpSession session)
    {
        // store the session context
        SessionContextManager sessionManager =
                ((WebContextsService) webBeansContext.getContextsService()).getSessionContextManager();
        SessionContext sessionContext = sessionManager.getSessionContextWithSessionId(session.getId());
        bag.put(ATTRIBUTE_SESSION_CONTEXT, sessionContext);

        // store all conversation contexts
        ConversationManager conversationManager = webBeansContext.getConversationManager();
        bag.put(ATTRIBUTE_CONVERSATION_CONTEXT_MAP, conversationManager.getConversationMapWithSessionId(session.getId()));

        if (LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.log(Level.FINE, "Beans for session [" + bag.getSessionId() + "] successfully stored in FailOverBag.");
        }
    }

    /**
     * Restores the session and conversation contexts from the given {@link FailOverBag}.
     * 
     * @param bag The {@link FailOverBag}.
     * @param session The current {@link HttpSession}.
     */
    @SuppressWarnings("unchecked")
    protected void restoreBeansFromFailOverBag(FailOverBag bag, HttpSession session)
    {
        try
        {
            // restore session context
            SessionContext sessionContext = (SessionContext) bag.get(ATTRIBUTE_SESSION_CONTEXT);

            if (sessionContext != null)
            {
                SessionContextManager sessionManager =
                        ((WebContextsService) webBeansContext.getContextsService()).getSessionContextManager();

                sessionManager.addNewSessionContext(session.getId(), sessionContext);
                sessionContext.setActive(true);
            }

            // restore conversation contexts
            Map<Conversation, ConversationContext> conversationContextMap =
                    (Map<Conversation, ConversationContext>) bag.get(ATTRIBUTE_CONVERSATION_CONTEXT_MAP);

            if (conversationContextMap != null && !conversationContextMap.isEmpty())
            {
                ConversationManager conversationManager = webBeansContext.getConversationManager();
                Iterator<Conversation> iterator = conversationContextMap.keySet().iterator();

                while (iterator.hasNext())
                {
                    Conversation conversation = iterator.next();
                    ConversationContext context = conversationContextMap.get(conversation);
                    conversationManager.addConversationContext(conversation, context);
                }
            }

            if (LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.log(Level.FINE, "Beans for session [" + bag.getSessionId() + "] from [" + bag.getJvmId() + "] successfully restored.");
            }
        }
        catch (Exception e)
        {
            String message = "Could not restore beans for session [" + bag.getSessionId()
                    + "] from [" + bag.getJvmId() + "]";
            LOGGER.log(Level.SEVERE, message, e);

            throw new WebBeansException(message, e);
        }
    }

    /**
     * Except the EJB remote stub, it is hard to handle other types of
     * resources. Here we delegate serialization/deserialization to the
     * application provided SerializationHandler.
     */
    public Object handleResource(Bean<?> bean, Object resourceObject, ObjectInput in, ObjectOutput out)
    {
        if (handler != null)
        {
            return handler.handleResource(bean, resourceObject, in, out,
                    (isPassivation()) ? SerializationHandlerV10.TYPE_PASSIVATION : SerializationHandlerV10.TYPE_FAILOVER);
        }
        return NOT_HANDLED;
    }

    /**
     * Get object input stream. Note, the stream should support deserialize
     * javassist objects.
     * 
     * @return custom object input stream.
     */
    public ObjectInputStream getObjectInputStream(InputStream in) throws IOException
    {
        return new OwbProxyObjectInputStream(in);
    }

    /**
     * Get object output stream. Note, the stream should support deserialize
     * javassist objects.
     * 
     * @return custom object output stream.
     */
    public ObjectOutputStream getObjectOutputStream(OutputStream out) throws IOException
    {
        return new ProxyObjectOutputStream(out);
    }

    public String getJvmId()
    {
        return JVM_ID;
    }

    public boolean isSupportFailOver()
    {
        return supportFailOver;
    }

    public void enableFailOverSupport(boolean supportFailOver)
    {
        this.supportFailOver = supportFailOver;
    }

    public boolean isSupportPassivation()
    {
        return supportPassivation;
    }

    public void enablePassivationSupport(boolean supportPassivation)
    {
        this.supportPassivation = supportPassivation;
    }

    public boolean isPassivation()
    {
        if (passivation.get() == null)
        {
            passivation.set(false);
        }

        return passivation.get();
    }
}
