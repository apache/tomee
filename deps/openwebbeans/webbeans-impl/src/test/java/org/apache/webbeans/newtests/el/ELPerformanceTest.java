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
package org.apache.webbeans.newtests.el;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.el.WebBeansELResolver;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.junit.Assert;
import org.junit.Test;

import javax.el.ELContext;

public class ELPerformanceTest extends AbstractUnitTest
{
    private final static int THREADS = 100;
    private final static int ITERATIONS = 200;


    private static Logger logger = WebBeansLoggerFacade.getLogger(ELPerformanceTest.class);

    /**
     * Test our bean creation for thread safety.
     */
    @Test
    public void testBeanCreation() throws Exception
    {
        Collection<Class<?>> classes = new ArrayList<Class<?>>();

        classes.add(SampleBean.class);
        classes.add(RequestBean.class);

        // we need this to enable the EL resolver at all!

        startContainer(classes);

        List<ParallelBeanStarter> strarters = new ArrayList<ParallelBeanStarter>();
        WebBeansELResolver resolver = new WebBeansELResolver();

        long start = System.nanoTime();

        for(int i=0; i<THREADS; i++)
        {
            ParallelBeanStarter starter = new ParallelBeanStarter(resolver);
            strarters.add(starter);
            starter.start();
        }
        
        for(ParallelBeanStarter starter : strarters)
        {
            starter.join();
        }        
        
        long end = System.nanoTime();

        logger.log(Level.INFO, "Executing {0} threads with {1} iterations took {2} ns", WebBeansLoggerFacade.args(THREADS, ITERATIONS, end - start));
        
        shutDownContainer();
    }
    
    private static class ParallelBeanStarter extends Thread
    {
        private WebBeansELResolver resolver;
        private ELContext elctx = new MockELContext();
        private static AtomicInteger n = new AtomicInteger(0);
        
        public ParallelBeanStarter(WebBeansELResolver resolver)
        {
            this.resolver = resolver;
        }
        
        @Override
        public void run()
        {
            WebBeansContext.currentInstance().getContextFactory().initRequestContext(null);
            WebBeansContext.currentInstance().getContextFactory().initSessionContext(null);

            try
            {
                for (int i = 0; i < ITERATIONS; i++)
                {
                    SampleBean sb = (SampleBean) resolver.getValue(elctx , null, "sampleBean");
                    sb.getRb().getY();
                    sb.getX();
                }
            }
            catch(RuntimeException e)
            {
                logger.log(Level.SEVERE, e.getMessage(), e);
                Assert.fail("got an exception: " + e.getMessage());
                throw e;
            }
            finally
            {
                WebBeansContext.currentInstance().getContextFactory().destroyRequestContext(null);
                WebBeansContext.currentInstance().getContextFactory().destroySessionContext(null);
            }
        }
    }
}
