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
package org.apache.webbeans.servlet;


import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.conversation.ConversationManager;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.util.WebBeansUtil;

import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebBeansConfigurationHttpSessionListener implements HttpSessionListener
{

    /**Logger instance*/
    private static final Logger logger = WebBeansLoggerFacade.getLogger(WebBeansConfigurationHttpSessionListener.class);

    /**Manages the container lifecycle*/
    protected ContainerLifecycle lifeCycle = null;

    public WebBeansConfigurationHttpSessionListener()
    {
        this.lifeCycle =  WebBeansContext.getInstance().getService(ContainerLifecycle.class);
    }

    /**
     * {@inheritDoc}
     */
    public void sessionCreated(HttpSessionEvent event)
    {
        try
        {
            if (logger.isLoggable(Level.FINE))
            {
                logger.log(Level.FINE, "Starting a session with session id : [{0}]", event.getSession().getId());
            }
            this.lifeCycle.getContextService().startContext(SessionScoped.class, event.getSession());
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE,
                    WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0020, event.getSession()));
            WebBeansUtil.throwRuntimeExceptions(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sessionDestroyed(HttpSessionEvent event)
    {
        if (logger.isLoggable(Level.FINE))
        {
            logger.log(Level.FINE, "Destroying a session with session id : [{0}]", event.getSession().getId());
        }
        this.lifeCycle.getContextService().endContext(SessionScoped.class, event.getSession());

        ConversationManager conversationManager = WebBeansContext.getInstance().getConversationManager();
        conversationManager.destroyConversationContextWithSessionId(event.getSession().getId());
    }
}
