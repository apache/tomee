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
package org.apache.webbeans.web.tests.interceptor;

import junit.framework.Assert;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.interceptors.beans.ApplicationScopedBean;
import org.apache.webbeans.newtests.interceptors.beans.RequestScopedBean;
import org.apache.webbeans.newtests.interceptors.common.TransactionInterceptor;
import org.apache.webbeans.web.tests.MockServletContext;
import org.apache.webbeans.web.tests.MockServletRequest;
import org.junit.Test;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.servlet.ServletRequestEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This test checks the performance of simple interceptor invocations.
 * It is usually enabled with only a few iteration cycles.
 */
@SuppressWarnings("unchecked")
public class CachedInterceptorHandlerTest extends AbstractUnitTest
{

    private static final int ITERATIONS = 100000;

    private static Logger logger = WebBeansLoggerFacade.getLogger(CachedInterceptorHandlerTest.class);


    @Test
    public void testInterceptorPerformance() throws Exception
    {
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(ApplicationScopedBean.class);
        beanClasses.add(RequestScopedBean.class);

        TransactionInterceptor.count = 0;

        startContainer(beanClasses, null);

        MockServletContext mockServletContext = new MockServletContext();
        MockServletRequest mockServletRequest = new MockServletRequest();
        ServletRequestEvent servletRequestEvent = new ServletRequestEvent(mockServletContext, mockServletRequest);



        long start = System.nanoTime();

        for (int req = 0; req < 10; req++)
        {
            getWebBeansContext().getContextsService().startContext(RequestScoped.class, servletRequestEvent);

            Set<Bean<?>> beans = getBeanManager().getBeans(RequestScopedBean.class);
            Assert.assertNotNull(beans);
            Bean<RequestScopedBean> bean = (Bean<RequestScopedBean>)beans.iterator().next();

            CreationalContext<RequestScopedBean> ctx = getBeanManager().createCreationalContext(bean);

            Object reference1 = getBeanManager().getReference(bean, RequestScopedBean.class, ctx);
            Assert.assertNotNull(reference1);

            Assert.assertTrue(reference1 instanceof RequestScopedBean);

            RequestScopedBean beanInstance1 = (RequestScopedBean)reference1;

            TransactionInterceptor.count = 0;

            for (int i= 1; i < ITERATIONS; i++)
            {
                beanInstance1.getMyService();
            }

            getWebBeansContext().getContextsService().endContext(RequestScoped.class, servletRequestEvent);
        }

        long end = System.nanoTime();

        logger.log(Level.INFO, "Executing {0} iterations took {1} ns", WebBeansLoggerFacade.args(ITERATIONS, end - start));

        shutDownContainer();

        if ((end - start) / 1e6 > ITERATIONS*10)
        {
            // if it takes longer than 1ms for each iteration, then this is really a performance blocker!
            Assert.fail("Performance test took more than 20 times longer than it should");
        }

    }

}
