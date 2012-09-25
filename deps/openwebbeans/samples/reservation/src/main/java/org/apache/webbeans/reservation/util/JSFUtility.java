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
package org.apache.webbeans.reservation.util;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 * Simple JSF Utility methods.
 */
public class JSFUtility
{

    /**
     * Getting current faces context
     * 
     * @return current context
     */
    public static FacesContext getCurrentContext()
    {
        return FacesContext.getCurrentInstance();
    }
    
    /**
     * Gets current http session
     * 
     * @return current http session
     */
    public static HttpSession getCurrentSession()
    {
        HttpSession session = (HttpSession) getCurrentContext().getExternalContext().getSession(false);
        
        return session;
    }
    
    /**
     * Creates and adds error message.
     * 
     * @param summary summary 
     * @param detail detail
     */
    public static void addInfoMessage(String summary, String detail)
    {
        addMessage(summary, detail, FacesMessage.SEVERITY_INFO);
    }
    
    /**
     * Creates and adds error message.
     * 
     * @param summary summary 
     * @param detail detail
     */
    public static void addErrorMessage(String summary, String detail)
    {
        addMessage(summary, detail, FacesMessage.SEVERITY_ERROR);
    }
    
    
    private static void addMessage(String summary, String detail, Severity severity)
    {
        FacesMessage facesMessage = new FacesMessage(severity,summary,detail);
        
        getCurrentContext().addMessage(null, facesMessage);
        
    }
}
