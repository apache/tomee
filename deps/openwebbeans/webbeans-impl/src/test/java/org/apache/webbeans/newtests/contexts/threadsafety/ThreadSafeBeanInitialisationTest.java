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
package org.apache.webbeans.newtests.contexts.threadsafety;

import junit.framework.Assert;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.spi.ContextsService;
import org.junit.Test;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This test will check if our bean creation mechanism is
 * thread safe.
 */
public class ThreadSafeBeanInitialisationTest extends AbstractUnitTest
{
    private final static Logger log = Logger.getLogger(ThreadSafeBeanInitialisationTest.class.getName());


    /**
     * Test our bean creation for thread safety.
     */
    @Test
    public void testBeanCreation() throws Exception
    {
        Collection<Class<?>> classes = new ArrayList<Class<?>>();

        // add a few random classes
        classes.add(LongInitApplicationBean.class);
        startContainer(classes);

        BeanManager bm = getBeanManager();

        ParallelBeanStarter bs1 = new ParallelBeanStarter(bm, getWebBeansContext().getContextsService());
        ParallelBeanStarter bs2 = new ParallelBeanStarter(bm, getWebBeansContext().getContextsService());
        ParallelBeanStarter bs3 = new ParallelBeanStarter(bm, getWebBeansContext().getContextsService());

        bs1.start();
        bs2.start();
        bs3.start();

        bs1.join();
        bs2.join();
        bs3.join();

        Assert.assertTrue(bs1.getLongInitBean().getThis() == bs2.getLongInitBean().getThis());
        Assert.assertTrue(bs1.getLongInitBean().getThis() == bs3.getLongInitBean().getThis());

        Assert.assertFalse(bs1.isFailed());
        Assert.assertFalse(bs2.isFailed());
        Assert.assertFalse(bs3.isFailed());
    }

    private static class ParallelBeanStarter extends Thread
    {
        private BeanManager bm;

        private LongInitApplicationBean longInitBean;
        private ContextsService cs;
        private boolean failed = true;

        public ParallelBeanStarter(BeanManager bm, ContextsService cs)
        {
            this.bm = bm;
            this.cs = cs;
        }

        public LongInitApplicationBean getLongInitBean()
        {
            return longInitBean;
        }

        public boolean isFailed()
        {
            return failed;
        }

        @Override
        public void run()
        {
            try
            {
                cs.startContext(RequestScoped.class, null);

                Set<Bean<?>> beans = bm.getBeans(LongInitApplicationBean.class);
                Assert.assertNotNull(beans);
                Assert.assertTrue(beans.size() == 1);
                Bean lrBean = beans.iterator().next();
                CreationalContext<LongInitApplicationBean> lrCreational = bm.createCreationalContext(lrBean);
                Assert.assertNotNull(lrCreational);

                longInitBean = (LongInitApplicationBean) bm.getReference(lrBean, LongInitApplicationBean.class, lrCreational);
                int i = longInitBean.getI();
                Assert.assertEquals(1, i);

                // all ok? then we didn't fail
                failed = false;
            }
            catch(Exception e)
            {
                log.log(Level.SEVERE, "ThreadSafeTest", e);
                Assert.fail();
            }
        }
    }
}
