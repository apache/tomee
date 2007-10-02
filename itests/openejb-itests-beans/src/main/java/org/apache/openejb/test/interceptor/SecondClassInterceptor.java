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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;


/**
 * @author <a href="mailto:goyathlay.geronimo@gmail.com">Prasad Kashyap</a>
 *
 * @version $Rev$ $Date$
 */
public class SecondClassInterceptor {
    
    
    /**
     * The interceptor method. 
     * This should intercept all business methods in this bean class
     * except those annotated with <code>@ExcludeClassInterceptors</code>
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
    public Object secondClassInterceptor(InvocationContext ctx) throws Exception {        
        Interceptor.profile(ctx, "secondClassInterceptor");
        return ctx.proceed();
    }
    
    /**
     * The interceptor method. 
     * This should intercept postConstruct of the bean
     * 
     * @param ctx - InvocationContext
     * 
     * @throws runtime exceptions.
     */    
    @PostConstruct
    public void secondClassInterceptorPostConstruct(InvocationContext ctx) throws Exception {
        Interceptor.profile(ctx, "secondClassInterceptorPostConstruct");
        ctx.proceed();
        return;
    }
    
    
    /**
     * The interceptor method. 
     * This should intercept postActivate of the bean
     * 
     * @param ctx - InvocationContext
     * 
     * @throws runtime exceptions.
     */    
    @PostActivate
    public void secondClassInterceptorPostActivate(InvocationContext ctx) throws Exception {
        Interceptor.profile(ctx, "secondClassInterceptorPostActivate");
        ctx.proceed();
        return;
    }
    
    /**
     * The interceptor method. 
     * This should intercept prePassivate of the bean.
     * 
     * @param ctx - InvocationContext
     * 
     * @throws runtime exceptions.
     */    
    @PrePassivate
    public void secondClassInterceptorPrePassivate(InvocationContext ctx) throws Exception {
        Interceptor.profile(ctx, "secondClassInterceptorPrePassivate");
        ctx.proceed();
        return;
    }
    
    /**
     * The interceptor method. 
     * This should intercept preDestroy of the bean.
     * 
     * @param ctx - InvocationContext
     * 
     * @throws runtime exceptions.
     */    
    @PreDestroy
    public void secondClassInterceptorPreDestroy(InvocationContext ctx) throws Exception {
        Interceptor.profile(ctx, "secondClassInterceptorPreDestroy");
        ctx.proceed();
        return;
    }

}
