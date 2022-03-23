/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.test.mdb;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.MessageDrivenContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.jms.Message;

import org.apache.openejb.test.entity.bmp.BasicBmpHome;
import org.apache.openejb.test.stateful.BasicStatefulHome;
import org.apache.openejb.test.stateless.BasicStatelessHome;

public class MdbInterceptor {
    @Resource
    private MessageDrivenContext mdbContext;
    @EJB(beanName = "BasicBmpBean")
    private BasicBmpHome bmpHome;
    @EJB(beanName = "BasicStatefulBean")
    private BasicStatefulHome statefulHome;
    @EJB(beanName = "BasicStatelessBean")
    private BasicStatelessHome statelessHome;


    @AroundInvoke
    public Object mdbInterceptor(final InvocationContext ctx) throws Exception {
        final Object[] objArr = ctx.getParameters();
        final Message msg = (Message) objArr[0];
        msg.clearProperties();
        msg.setBooleanProperty("ClassLevelBusinessMethodInterception", true);
        ctx.setParameters(objArr);
        return ctx.proceed();
    }

    @PreDestroy
    public void interceptRemove(final InvocationContext ctx) throws Exception {
        ctx.proceed();
    }

    @PostConstruct
    public void postConstruct(final InvocationContext ctx) throws Exception {
        InterceptorMdbBean.classLevelCreateMethodInterception = true;
        ctx.proceed();
    }


}
