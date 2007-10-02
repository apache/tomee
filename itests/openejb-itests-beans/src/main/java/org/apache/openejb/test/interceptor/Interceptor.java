/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.test.interceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.interceptor.InvocationContext;

import org.apache.openejb.test.SuperInterceptedBean;

/**
 * @author <a href="mailto:goyathlay.geronimo@gmail.com">Prasad Kashyap</a>
 *
 * @version $Rev$ $Date$
 */
public class Interceptor {
    
    /*@Resource
    static SessionContext sessionContext;*/
    
    /**
     * This interceptor creates/updates an inner map for every method that it intercepts.
     * The inner map contains the array of method parameters in the key PARAMETERS.
     * The inner map contains the list of interceptor methods in the key INTERCEPTORS.
     * The inner map is put back into the contextData against the method name as the key.
     * 
     * @param ctx - InvocationContext
     * @param interceptorName
     * @return contextData - the contextData which now has been filled with a hashmap of hashmap.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> profile(InvocationContext ctx, String interceptorName) {
        /*if (sessionContext != null) {
            System.out.println(sessionContext.lookup("java:comp/env"));        
        }
        else {
            System.out.println("SessionContext is null");
        }*/


        Map<String, Object> ctxData = ctx.getContextData();

        String KEY;
        if (ctx.getMethod() != null) {
            KEY = ctx.getMethod().getName();    
        }        
        else {
            KEY = ((SuperInterceptedBean)ctx.getTarget()).getClass().getSimpleName();
        }
        System.out.print("Intercepting " + KEY);
        
        Map<String, Object> innerMap = (HashMap<String, Object>) ctxData.get(KEY);
        innerMap = updateInterceptorsList(innerMap, interceptorName);

        // don't try to get parameters for call back methods (you'll get an IllegalStateException)
        if (ctx.getMethod() != null) {
            Object[] params = ctx.getParameters();
            innerMap.put("PARAMETERS", params);
        }

        ctxData.put(KEY, innerMap);
        
        return ctxData; 
    }

    /**
     * This is invoked by the lifecycle interceptor callback methods that are defined inside a bean.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> profile(SuperInterceptedBean bean, String interceptorName) {
        Map<String, Object> ctxData = new HashMap<String, Object>();
        
        String KEY = bean.getClass().getSimpleName();
        System.out.print("Intercepting " + KEY);
        
        Map<String, Object> innerMap = (HashMap<String, Object>) ctxData.get(KEY);
        innerMap = updateInterceptorsList(innerMap, interceptorName);
        
        ctxData.put(KEY, innerMap);
        return ctxData;        
    }

    /**
     * @param innerMap
     * @param interceptorName
     * @return innerMap
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> updateInterceptorsList(Map<String, Object> innerMap, String interceptorName) {
        if(innerMap == null) {
            innerMap = new HashMap<String, Object>();
        }        
        
        ArrayList<String> interceptorsList = (ArrayList<String>) innerMap.get("INTERCEPTORS");
        if(interceptorsList == null) {
            interceptorsList = new ArrayList<String>();            
        }

        System.out.println(" by " + interceptorName + "()");
        interceptorsList.add(interceptorName);
        innerMap.put("INTERCEPTORS", interceptorsList);
        
        return innerMap;
    }

}
