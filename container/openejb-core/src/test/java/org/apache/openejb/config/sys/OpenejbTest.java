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
package org.apache.openejb.config.sys;

import junit.framework.TestCase;
import org.apache.openejb.config.Service;
import org.apache.openejb.config.SystemProperty;
import org.xml.sax.InputSource;

import jakarta.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @version $Rev$ $Date$
 */
public class OpenejbTest extends TestCase {

    private int i = 100;

    public void test() throws Exception {
        final Openejb openejb = new Openejb();

        openejb.setSecurityService(fill(new SecurityService("Orange")));
        openejb.setTransactionManager(fill(new TransactionManager("Yellow")));
        openejb.setConnectionManager(fill(new ConnectionManager("Purple")));
        openejb.setProxyFactory(fill(new ProxyFactory("Brown")));

        openejb.getContainer().add(fill(new Container("Red")));
        openejb.getContainer().add(fill(new Container("Green")));
        openejb.getContainer().add(fill(new Container("Blue")));
        openejb.getJndiProvider().add(fill(new JndiProvider("Red")));
        openejb.getJndiProvider().add(fill(new JndiProvider("Green")));
        openejb.getJndiProvider().add(fill(new JndiProvider("Blue")));
        openejb.getResource().add(fill(new Resource("Red")));
        openejb.getResource().add(fill(new Resource("Green")));
        openejb.getResource().add(fill(new Resource("Blue")));

        openejb.getDeployments().add(dir("square"));
        openejb.getDeployments().add(jar("circle"));
        openejb.getDeployments().add(jar("triangle"));
        openejb.getSystemProperties().add(new SystemProperty().name("a-sys").value("a-val"));

        final byte[] expected = bytes(openejb);

        final InputSource inputSource = new InputSource(new ByteArrayInputStream(expected));

        final long start = System.currentTimeMillis();
        final byte[] actual = bytes(SaxOpenejb.parse(inputSource));
        System.out.println(System.currentTimeMillis() - start);

        assertEquals(new String(expected), new String(actual));
    }

    private byte[] bytes(final Openejb openejb) throws JAXBException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        JaxbOpenejb.marshal(Openejb.class, openejb, out);


        return out.toByteArray();
    }

    private Deployments dir(final String s) {
        final Deployments deployments = new Deployments();
        deployments.setDir(s);
        return deployments;
    }

    private Deployments jar(final String s) {
        final Deployments deployments = new Deployments();
        deployments.setFile(s);
        return deployments;
    }

    private Resource fill(final Resource resource) {
        resource.setJndi("JNDI." + resource.getId() + i);
        return (Resource) fill((Service) resource);
    }

    private <T extends Service> T fill(final T service) {
        final String text = service.getId() + "." + i;
        if (service.getJar() == null) service.setJar("JAR." + text);
        if (service.getProvider() == null) service.setProvider("PROVIDER." + text);
        if (service.getType() == null) service.setType("TYPE." + text);
        if (service.getProperties().size() == 0) {
            service.getProperties().setProperty("P1." + text, "V1." + text);
            service.getProperties().setProperty("P2." + text, "V2." + text);
        }

        i++;

        return service;
    }

}
