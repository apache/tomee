/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.itests.trimmedbeans;

import java.util.Properties;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.core.LocalInitialContext;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.webbeans.config.WebBeansContext;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * This test checks that CDI does not pick up non annotated beans in
 * a trimmed bean archive.
 */
public class TrimmedBeansTest {

    @Test
    public void startupBeanWithAmbiguousResolutionException() throws Exception {
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        props.setProperty(LocalInitialContext.ON_CLOSE, LocalInitialContext.Close.DESTROY.name());
        this.getClass().getClassLoader().loadClass("org.apache.openejb.server.ServiceManager");
        props.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");

        Context context = new InitialContext(props);
        BeanManager bm = WebBeansContext.currentInstance().getBeanManagerImpl();

        assertFalse(bm.getBeans(BeanA.class).isEmpty());
        assertFalse(bm.getBeans(BeanB.class).isEmpty());
        assertTrue(bm.getBeans(NotPickedUpBean.class).isEmpty());
    }
}
