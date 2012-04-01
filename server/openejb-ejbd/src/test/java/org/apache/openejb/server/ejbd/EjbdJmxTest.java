/*
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
package org.apache.openejb.server.ejbd;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceManager;
import org.junit.Test;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class EjbdJmxTest {

    @Test
    public void test() throws Exception {
        final MBeanServer server = LocalMBeanServer.get();

        OpenEJB.init(new Properties());

        final Properties p = new Properties();
        p.put("server", "org.apache.openejb.server.ejbd.EjbServer");
        p.put("bind", "127.0.0.1");
        p.put("port", "0");
        p.put("disabled", "false");
        p.put("threads", "10");
        p.put("backlog", "200");
        p.put("discovery", "ejb:ejbd://{bind}:{port}");
        final ServerService service = ServiceManager.manage("ejbd", p, new EjbServer());
        service.init(p);
        service.start();

        ServiceManager.register("ejbd", service, server);

        ObjectName invocationsName = new ObjectName("openejb:type=ServerService,name=ejbd");

        MBeanInfo beanInfo = server.getMBeanInfo(invocationsName);

        for (MBeanAttributeInfo info : beanInfo.getAttributes()) {
            System.out.println(info);
        }
    }
}
