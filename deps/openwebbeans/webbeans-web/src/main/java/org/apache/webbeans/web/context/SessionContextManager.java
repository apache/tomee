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
package org.apache.webbeans.web.context;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.webbeans.context.SessionContext;
import org.apache.webbeans.util.Asserts;

/**
 * Session context manager.
 * <p>
 * Each session is identified by the session id.
 * </p>
 * @version $Rev$ $Date$
 *
 */
public class SessionContextManager
{
    /** Session id to SessionContext map*/
    private final Map<String, SessionContext> sessionContexts;

    
    /**
     * Creates a new session context manager.
     */
    public SessionContextManager()
    {
        sessionContexts = new ConcurrentHashMap<String, SessionContext>();        
    }

    /**
     * Adds new session context for the given session id.
     * @param sessionId session id
     * @param context session context
     */
    public void addNewSessionContext(String sessionId, SessionContext context)
    {
        Asserts.assertNotNull(sessionId, "sessionId parameter can not be null");
        Asserts.assertNotNull(context, "context parameter can not be null");

        sessionContexts.put(sessionId, context);
    }

    /**
     * Gets session context related with given session id.
     * @param sessionId session id
     * @return session context related with given session id
     */
    public SessionContext getSessionContextWithSessionId(String sessionId)
    {
        Asserts.assertNotNull(sessionId, "sessionId parameter can not be null");

        return sessionContexts.get(sessionId);
    }
    
    /**
     * Destroy session context with given id.
     * @param sessionId session id
     */
    public void destroySessionContextWithSessionId(String sessionId)
    {
        SessionContext sessionContext = this.sessionContexts.remove(sessionId);
        if(sessionContext != null)
        {
            sessionContext.destroy();
        }
    }
    
    /**
     * Destroys all sessions.
     */
    public void destroyAllSessions()
    {
        //Destroy all contexts
        Collection<SessionContext> allSessionContexts = this.sessionContexts.values();
        if(allSessionContexts != null && allSessionContexts.size() > 0)
        {
            for(SessionContext sessionContext : allSessionContexts)
            {
                sessionContext.destroy();
            }

        //Clear map
        allSessionContexts.clear();
        }
    }
}
