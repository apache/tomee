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
package org.apache.openejb.test.stateful;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Stateful;
import javax.interceptor.AroundInvoke;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

import org.apache.openejb.test.SuperInterceptedBean;
import org.apache.openejb.test.interceptor.ClassInterceptor;
import org.apache.openejb.test.interceptor.Interceptor;
import org.apache.openejb.test.interceptor.MethodInterceptor;

/**
 * 
 *
 * @version $Rev$ $Date$
 */
@Stateful(name="StatefulIntercepted")
@Interceptors({ClassInterceptor.class})
public class StatefulInterceptedBean extends SuperInterceptedBean 
                  implements BasicStatefulInterceptedLocal, BasicStatefulInterceptedRemote {
    
    private static Map<String, Object> contextData = new HashMap<String, Object>(); 

    /**
     * A simple dummy business method to concat 2 strings
     * @see org.apache.openejb.test.Stateful.fulInterceptedLocal#concat(java.lang.String, java.lang.String)
     */
    public String concat(String str1, String str2) {
        return str1.concat(str2);
    }

    /**
     * A simple dummy busines method to reverse a string
     * @see org.apache.openejb.test.Stateful.fulInterceptedLocal#reverse(java.lang.String)
     */
    @Interceptors({MethodInterceptor.class})
    public String reverse(String str) {
        System.out.println("Reversing " + str);
        StringBuffer b = new StringBuffer(str);
        return b.reverse().toString();
    }
    
    /**
     * <code>ClassInterceptor</code> should not intercept this.
     * 
     * @return the contextData
     */
    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    public Map<String, Object> getContextData() {
        return contextData;
    }

    /**
     * @param contextData the contextData to set
     */
    private void setContextData(Map<String, Object> ctxData) {
        StatefulInterceptedBean.contextData.putAll(ctxData);
    }

    /**
     * The interceptor method. 
     * This should intercept all business methods in this bean class.
     * It cannot exclude even those annotated with <code>@ExcludeClassInterceptors</code>
     * 
     * @param ctx - InvocationContext
     * 
     * @return - the result of the next method invoked. If a method returns void, proceed returns null. 
     * For lifecycle callback interceptor methods, if there is no callback method defined on the bean class, 
     * the invocation of proceed in the last interceptor method in the chain is a no-op, and null is returned. 
     * If there is more than one such interceptor method, the invocation of proceed causes the container to execute those methods in order.
     * 
     * @throws runtime exceptions or application exceptions that are allowed in the throws clause of the business method.
     */
    @AroundInvoke
    public Object inBeanInterceptor(InvocationContext ctx) throws Exception {
        Map<String, Object> ctxData = Interceptor.profile(ctx, "inBeanInterceptor");
        setContextData(ctxData);
    
        return ctx.proceed();
    }

    /**
     * The interceptor method. 
     * This should intercept postConstruct of the bean
     * 
     * @throws runtime exceptions.
     */    
    @PostConstruct
    public void inBeanInterceptorPostConstruct() throws Exception {
        Map<String, Object> ctxData = Interceptor.profile(this, "inBeanInterceptorPostConstruct");
        setContextData(ctxData);
        return;
    }
    
    
    /**
     * The interceptor method. 
     * This should intercept postActivate of the bean
     * 
     * @throws runtime exceptions.
     */    
    @PostActivate
    public void inBeanInterceptorPostActivate() throws Exception {
        Map<String, Object> ctxData = Interceptor.profile(this, "inBeanInterceptorPostActivate");
        setContextData(ctxData);
        return;
    }
    
    /**
     * The interceptor method. 
     * This should intercept prePassivate of the bean.
     * 
     * @throws runtime exceptions.
     */    
    @PrePassivate
    public void inBeanInterceptorPrePassivate() throws Exception {
        Map<String, Object> ctxData = Interceptor.profile(this, "inBeanInterceptorPrePassivate");
        setContextData(ctxData);
        return;
    }
    
    /**
     * The interceptor method. 
     * This should intercept preDestroy of the bean.
     * 
     * @throws runtime exceptions.
     */    
    @PreDestroy
    public void inBeanInterceptorPreDestroy() throws Exception {
        Map<String, Object> ctxData = Interceptor.profile(this, "inBeanInterceptorPreDestroy");
        setContextData(ctxData);
        return;
    }
    
    

}
