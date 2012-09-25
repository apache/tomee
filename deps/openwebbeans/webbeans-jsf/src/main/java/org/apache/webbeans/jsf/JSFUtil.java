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
package org.apache.webbeans.jsf;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.webbeans.util.Asserts;


public final class JSFUtil
{
    private JSFUtil()
    {

    }

    public static FacesContext getCurrentFacesContext()
    {
        return FacesContext.getCurrentInstance();
    }

    public static ExternalContext getExternalContext()
    {
        FacesContext context = getCurrentFacesContext();
        
        if(context != null)
        {
            return context.getExternalContext();
        }
        
        return null;
    }

    public static HttpSession getSession()
    {
        ExternalContext externalContext = getExternalContext();
        
        if(externalContext != null)
        {
            return (HttpSession) externalContext.getSession(true);
        }
        
        return null; 
    }

    public static String getRedirectViewIdWithCid(String redirectId, String cid)
    {
        Asserts.assertNotNull(redirectId, "redirectId parameter can not be null");        

        StringBuilder buffer = new StringBuilder(redirectId);
        int index = buffer.lastIndexOf("/");        
        
        String remainder = buffer.substring(index+1, buffer.length());
        int indexOfQuery = remainder.indexOf('?');
        
        StringBuilder result = new StringBuilder();
        if(indexOfQuery != -1)
        {
            result.append(buffer.substring(0,index+1));
            result.append(remainder.substring(0,indexOfQuery+1));
            result.append("cid");
            result.append("=");
            result.append(cid);
            result.append("&");
            result.append(remainder.substring(indexOfQuery+1, remainder.length()));            
        }
        else
        {
            int pathIndex = remainder.indexOf("#");
            
            if(pathIndex == -1)
            {
                result.append(buffer.substring(0,index+1));
                result.append(remainder);
                result.append("?");
                result.append("cid");
                result.append("=");
                result.append(cid);
            }
            else
            {
                result.append(buffer.substring(0,index+1));
                result.append(remainder.substring(0,pathIndex));
                result.append("?");
                result.append("cid");
                result.append("=");
                result.append(cid);
                result.append(remainder.substring(pathIndex,remainder.length()));
            }            
        }
        
        
        return result.toString();
            
    }

    public static UIViewRoot getViewRoot()
    {
        FacesContext context = getCurrentFacesContext();
        
        if(context != null)
        {
            return context.getViewRoot();
        }
        
        return null;
    }
    
    
    public static String getJSFRequestParameter(String parameterName)
    {   
        ExternalContext ec = getExternalContext();
        if(ec != null)
        {
            return ec.getRequestParameterMap().get(parameterName);
        }
        
        return null;
    }

    public static String getConversationId()
    {
        return getJSFRequestParameter("cid");
    }

}
