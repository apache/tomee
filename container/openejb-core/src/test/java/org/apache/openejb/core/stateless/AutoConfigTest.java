/**
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
package org.apache.openejb.core.stateless;

import junit.framework.TestCase;

import javax.annotation.Resource;
import java.util.Map;
import java.util.HashMap;

import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.ResourceReferenceInfo;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.EjbJar;

/**
 * @version $Revision: 570984 $ $Date: 2007-08-29 16:42:12 -0700 (Wed, 29 Aug 2007) $
 */
public class AutoConfigTest extends TestCase {

    public void test() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        assembler.createResource(config.configureService(new org.apache.openejb.config.sys.Resource("defaultDataSource", null, "DataSource"), ResourceInfo.class));
        assembler.createResource(config.configureService(new org.apache.openejb.config.sys.Resource("yellowDataSource", null, "DataSource"), ResourceInfo.class));
        assembler.createResource(config.configureService(new org.apache.openejb.config.sys.Resource("PurpleDataSource", null, "DataSource"), ResourceInfo.class));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(WidgetBean.class));

        EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);

        EnterpriseBeanInfo beanInfo = ejbJarInfo.enterpriseBeans.get(0);

        Map<String, ResourceReferenceInfo> refs = new HashMap<String,ResourceReferenceInfo>();
        for (ResourceReferenceInfo ref : beanInfo.jndiEnc.resourceRefs) {
            refs.put(ref.referenceName.replaceAll(".*/",""), ref);
        }

        ResourceReferenceInfo info;
        info = refs.get("yellowDataSource");
        assertNotNull(info);
        assertEquals("yellowDataSource", info.resourceID);

        info = refs.get("orangeDataSource");
        assertNotNull(info);
        assertEquals("defaultDataSource", info.resourceID);

        info = refs.get("purpleDataSource");
        assertNotNull(info);
        assertEquals("PurpleDataSource", info.resourceID);

    }

    public static interface Widget {
    }


    public static class WidgetBean implements Widget {

        @Resource javax.jms.Queue redQueue;
        @Resource javax.jms.Queue blueQueue;
        @Resource javax.jms.Queue greenQueue;

        @Resource javax.sql.DataSource yellowDataSource;
        @Resource javax.sql.DataSource orangeDataSource;
        @Resource javax.sql.DataSource purpleDataSource;
    }
}
