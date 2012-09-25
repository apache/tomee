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
package org.apache.webbeans.reservation.security;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.webbeans.reservation.entity.User;
import org.apache.webbeans.reservation.session.SessionTracker;

public class JSFSecurityPhaseListener implements PhaseListener
{

    private static final long serialVersionUID = -1308051590485364148L;
    
    public void afterPhase(PhaseEvent event)
    {        
        FacesContext context = event.getFacesContext();
        String pathInfo = context.getExternalContext().getRequestServletPath();
        
        if(pathInfo.startsWith("/admin") || pathInfo.startsWith("/user"))
        {
            SessionTracker tracker = null;
            User user = null;
            try
            {
                tracker = (SessionTracker)context.getApplication().evaluateExpressionGet(context, "#{sessionTracker}", SessionTracker.class);
                user = tracker.getUser();
                
            }
            catch(Exception e)
            {
                //Tracker is null
                System.out.println("Context is not active");
            }
            
            if(tracker == null || user == null)
            {
                try
                {
                    context.getExternalContext().redirect(context.getExternalContext().getRequestContextPath() + "/login.jsf");
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
            
        }
         
        
    }

    public void beforePhase(PhaseEvent event)
    {
    }

    public PhaseId getPhaseId()
    {
        return PhaseId.RESTORE_VIEW;
    }

}
