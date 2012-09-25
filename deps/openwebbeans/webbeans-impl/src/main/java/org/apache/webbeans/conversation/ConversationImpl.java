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
package org.apache.webbeans.conversation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;

import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.OpenWebBeansConfiguration;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ConversationContext;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.util.Asserts;

/**
 * Implementation of the {@link Conversation} interface.
 * @version $Rev$ $Date$
 *
 */
public class ConversationImpl implements Conversation, Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 8511063860333431722L;

    /**Logger instance*/
    private final static Logger logger = WebBeansLoggerFacade.getLogger(ConversationImpl.class);
    
    /**Conversation id*/
    private String id;

    /**Transient or not. Transient conversations are destroyed at the end of JSF request*/
    private boolean isTransient = true;

    /**Default timeout is 30mins*/
    private long timeout;

    /**Id of the session that this conversation is created*/
    private String sessionId;

    /**Active duration of the conversation*/
    private long activeTime = 0L;
    
    /**Generating ids*/
    private static AtomicInteger conversationIdGenerator = new AtomicInteger(0);
    
    /**This instance is under used*/
    private AtomicBoolean inUsed = new AtomicBoolean(false);

    private transient WebBeansContext webBeansContext;

    /**
     * Default constructor. Used for proxies.
     */
    public ConversationImpl()
    {
        super();
    }

    public ConversationImpl(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
        try
        {
            timeout = Long.parseLong(this.webBeansContext.getOpenWebBeansConfiguration().
                    getProperty(OpenWebBeansConfiguration.CONVERSATION_TIMEOUT_INTERVAL, "1800000"));   
        }
        catch(NumberFormatException e)
        {
            timeout = 30 * 60 * 1000;
        }
    }

    /**
     * Creates a new conversation instance. Id is not
     * set until conversation is begin.
     * @param sessionId
     * @param webBeansContext
     */
    public ConversationImpl(String sessionId, WebBeansContext webBeansContext)
    {
        Asserts.assertNotNull(sessionId);

        this.webBeansContext = webBeansContext;

        try
        {
            timeout = Long.parseLong(this.webBeansContext.getOpenWebBeansConfiguration().
                    getProperty(OpenWebBeansConfiguration.CONVERSATION_TIMEOUT_INTERVAL, "1800000"));   
        }
        catch(NumberFormatException e)
        {
            timeout = 30 * 60 * 1000;
        }
        
        this.sessionId = sessionId;
    }
    
    /**
     * {@inheritDoc}
     */
    public void begin()
    {
        //Transient state
        if(isTransient)
        {
            isTransient = false;
            id = Integer.toString(conversationIdGenerator.incrementAndGet());
            updateTimeOut();

            //Conversation manager
            ConversationManager manager = webBeansContext.getConversationManager();
            try
            {
                //Gets current conversation context instance.
                //Each conversation has its own conversation context instance.
                //Sets at the beginning of each JSF request.
                manager.addConversationContext(this, (ConversationContext) webBeansContext.getBeanManagerImpl().getContext(ConversationScoped.class));
                
            }
            catch(Exception e)
            {
                //TCK tests
                manager.addConversationContext(this, new ConversationContext());
            }            
        }
        //Already started conversation.
        else
        {
            logger.log(Level.WARNING, OWBLogConst.WARN_0003, id);
            throw new IllegalStateException();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void begin(String id)
    {   
        //Look at other conversation, that may collate with this is
        final ConversationManager conversationManager = webBeansContext.getConversationManager();
        if(conversationManager.isConversationExistWithGivenId(id))
        {
            throw new IllegalArgumentException("Conversation with id=" + id + " is already exist!");
        }
        
        //Transient state
        if(isTransient)
        {
            isTransient = false;
            this.id = id;
            updateTimeOut();
            conversationManager.addConversationContext(this, (ConversationContext) webBeansContext.getBeanManagerImpl().getContext(ConversationScoped.class));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void end()
    {
        if(!isTransient)
        {
            isTransient = true;

            webBeansContext.getConversationManager().removeConversation(this);
        }
        else
        {
            logger.log(Level.WARNING, OWBLogConst.WARN_0004, id);
            throw new IllegalStateException(toString() + " has already ended");
        }
    }
    
    
    /**
     * @return the inUsed
     */
    public AtomicBoolean getInUsed()
    {
        return inUsed;
    }

    /**
     * @param inUsed the inUsed to set
     */
    public void setInUsed(boolean inUsed)
    {
        this.inUsed.set(inUsed);
    }
    
    /**
     * Sets transient.
     * @param value transient value
     */
    public void setTransient(boolean value)
    {
        isTransient = value;
    }
    
    /**
     * {@inheritDoc}
     */    
    public String getId()
    {
        return id;
    }

    /**
     * {@inheritDoc}
     */    
    public long getTimeout()
    {
        return timeout;
    }

    /**
     * {@inheritDoc}
     */    
    public boolean isTransient()
    {
        return isTransient;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setTimeout(long milliseconds)
    {
        timeout = milliseconds;
    }

    /**
     * Gets session id.
     * @return conversation session id
     */
    public String getSessionId()
    {
        return sessionId;
    }

    /**
     * @return the creationTime
     */
    public long getActiveTime()
    {
        return activeTime;
    }


    /**
     * Update conversation timeout value.
     */
    public void updateTimeOut()
    {
        activeTime = System.currentTimeMillis();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj == null)
        {
            return false;
        }

        if (getClass() != obj.getClass())
        {
            return false;
        }

        final ConversationImpl other = (ConversationImpl) obj;
        if (id == null)
        {
            if (other.id != null)
            {
                return false;
            }
        }
        else if (!id.equals(other.id))
        {
            return false;
        }
        if (sessionId == null)
        {
            if (other.sessionId != null)
            {
                return false;
            }
        }
        else if (!sessionId.equals(other.sessionId))
        {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Conversation with id [ ");
        builder.append(id);
        builder.append(" ]");
        
        return builder.toString();
    }

    /**
     * We need this for restoring our WebBeansContext on de-serialisation
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        webBeansContext = WebBeansContext.currentInstance();
        in.defaultReadObject();
    }

}
