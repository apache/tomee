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
package org.apache.openejb.config;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.StatelessBean;

import jakarta.annotation.Resource;
import jakarta.ejb.EJBContext;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.TimeUnit;

/**
 * @version $Revision$ $Date$
 */
public class InjectionTest extends TestCase {

    public void testInjections() throws Exception {
        final InitialContext ctx = new InitialContext();

        final Object object = ctx.lookup("WidgetBeanLocal");

        assertTrue("instanceof widget", object instanceof Widget);

        final Widget widget = (Widget) object;

        // injected via annotations
        assertEquals("2", widget.getString());
        assertEquals(3.0D, widget.getDouble());
        assertEquals(new Long(4), widget.getLong());
        assertEquals(5f, widget.getFloat());
        assertEquals(new Integer(6), widget.getInteger());
        assertEquals(new Short((short) 7), widget.getShort());
        assertEquals(Boolean.FALSE, widget.getBoolean());
        assertEquals(new Character('9'), widget.getCharacter());
        assertEquals(Widget.class, widget.getMyClass());
        assertEquals(TimeUnit.HOURS, widget.getTimeUnit());

        // injected via DD
        assertEquals(true, widget.getInjectedBoolean());
        assertEquals(true, widget.lookup("injectedBoolean"));
        assertNotNull(widget.getInjectedContext());
    }

    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        final StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.setProperty("TimeOut", "10");
        statelessContainerInfo.properties.setProperty("MaxSize", "0");
        statelessContainerInfo.properties.setProperty("StrictPooling", "false");
        assembler.createContainer(statelessContainerInfo);

        // Setup the descriptor information

        final StatelessBean bean = new StatelessBean(WidgetBean.class);
        bean.addBusinessLocal(Widget.class.getName());
        bean.addBusinessRemote(RemoteWidget.class.getName());

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        bean.getEnvEntry().add(new EnvEntry(name("myString"), "java.lang.String", "2"));
        bean.getEnvEntry().add(new EnvEntry(name("myDouble"), "java.lang.Double", "3.0"));
        bean.getEnvEntry().add(new EnvEntry(name("myLong"), "java.lang.Long", "4"));
        bean.getEnvEntry().add(new EnvEntry(name("myFloat"), "java.lang.Float", "5"));
        bean.getEnvEntry().add(new EnvEntry(name("myInteger"), "java.lang.Integer", "6"));
        bean.getEnvEntry().add(new EnvEntry(name("myShort"), "java.lang.Short", "7"));
        bean.getEnvEntry().add(new EnvEntry(name("myBoolean"), "java.lang.Boolean", "false"));
        bean.getEnvEntry().add(new EnvEntry(name("myByte"), "java.lang.Byte", "8"));
        bean.getEnvEntry().add(new EnvEntry(name("myCharacter"), "java.lang.Character", "9"));
        bean.getEnvEntry().add(new EnvEntry(name("myClass"), "java.lang.Class", Widget.class.getName()));
        bean.getEnvEntry().add(new EnvEntry(name("myTimeUnit"), TimeUnit.class.getName(), "HOURS"));

        final EnvEntry entry = new EnvEntry("injectedBoolean", (String) null, "true");
        entry.getInjectionTarget().add((new InjectionTarget(WidgetBean.class.getName(), "injectedBoolean")));
        bean.getEnvEntry().add(entry);

        final ResourceEnvRef resourceEnvRef = new ResourceEnvRef("injectedContext", (String) null);
        resourceEnvRef.getInjectionTarget().add((new InjectionTarget(WidgetBean.class.getName(), "injectedContext")));
        bean.getResourceEnvRef().add(resourceEnvRef);

        assembler.createApplication(config.configureApplication(ejbJar));
    }

    @Override
    protected void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    private String name(final String name) {
        return "java:comp/env/" + WidgetBean.class.getName() + "/" + name;
    }

    public static interface Widget {
        String getString();

        Double getDouble();

        Long getLong();

        Float getFloat();

        Short getShort();

        Integer getInteger();

        Boolean getBoolean();

        Character getCharacter();

        Byte getByte();

        Class getMyClass();

        TimeUnit getTimeUnit();

        Object lookup(String name) throws NamingException;

        boolean getInjectedBoolean();

        EJBContext getInjectedContext();
    }

    public static interface RemoteWidget extends Widget {
    }

    @SuppressWarnings("FieldMayBeFinal")
    @Stateless
    public static class WidgetBean implements Widget, RemoteWidget {

        private SessionContext sessionContext;

        @Resource
        private String myString = "1";

        @Resource
        private Double myDouble = 1.0D;

        @Resource
        private Long myLong = 1L;

        @Resource
        private Float myFloat = 1.0F;

        @Resource
        private Integer myInteger = 1;

        @Resource
        private Short myShort = (short) 1;

        @Resource
        private Boolean myBoolean = true;

        @Resource
        private Byte myByte = (byte) 1;

        @Resource
        private Character myCharacter = '1';

        @Resource
        private Class myClass = Object.class;

        @Resource
        private TimeUnit myTimeUnit = TimeUnit.DAYS;

        // injected via DD
        private boolean injectedBoolean = false;

        // injected via DD
        private EJBContext injectedContext;

        public WidgetBean() {
        }

        @Resource
        public void setSessionContext(final SessionContext sessionContext) {
            this.sessionContext = sessionContext;
        }

        public Object lookup(final String name) throws NamingException {
            return sessionContext.lookup(name);
        }

        public Boolean getBoolean() {
            return myBoolean;
        }

        public Byte getByte() {
            return myByte;
        }

        public Character getCharacter() {
            return myCharacter;
        }

        public Double getDouble() {
            return myDouble;
        }

        public Float getFloat() {
            return myFloat;
        }

        public Integer getInteger() {
            return myInteger;
        }

        public Long getLong() {
            return myLong;
        }

        public Class getMyClass() {
            return myClass;
        }

        public Short getShort() {
            return myShort;
        }

        public String getString() {
            return myString;
        }

        public TimeUnit getTimeUnit() {
            return myTimeUnit;
        }

        public boolean getInjectedBoolean() {
            return injectedBoolean;
        }

        public EJBContext getInjectedContext() {
            return injectedContext;
        }
    }
}
