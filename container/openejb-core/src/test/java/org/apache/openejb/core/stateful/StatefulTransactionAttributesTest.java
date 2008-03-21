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
package org.apache.openejb.core.stateful;

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.MethodAttributeInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.TransAttribute;
import org.apache.openejb.jee.MethodIntf;
import org.apache.openejb.jee.StatefulBean;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.Init;
import javax.ejb.Remove;
import javax.naming.InitialContext;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class StatefulTransactionAttributesTest extends TestCase {

    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean(Color.class));
        ejbJar.addEnterpriseBean(new StatefulBean(Red.class));
        ejbJar.addEnterpriseBean(new StatefulBean(Crimson.class));
        ejbJar.addEnterpriseBean(new StatefulBean(Scarlet.class));
        List<ContainerTransaction> declared = ejbJar.getAssemblyDescriptor().getContainerTransaction();

        declared.add(new ContainerTransaction(TransAttribute.REQUIRED, "*", "Crimson", "*"));
        declared.add(new ContainerTransaction(TransAttribute.REQUIRES_NEW, "*", "Crimson", "create"));
        ContainerTransaction o = new ContainerTransaction(TransAttribute.SUPPORTS, "*", "Crimson", "create");
        o.getMethod().get(0).setMethodIntf(MethodIntf.HOME);
        declared.add(o);
        declared.add(new ContainerTransaction(TransAttribute.REQUIRES_NEW, "*", "Crimson", "remove"));
        declared.add(new ContainerTransaction(TransAttribute.REQUIRES_NEW, Color.class.getName(), "Scarlet", "*"));
        declared.add(new ContainerTransaction(TransAttribute.NEVER, Red.class.getName(), "Scarlet", "red"));
        declared.add(new ContainerTransaction(TransAttribute.REQUIRED, "Scarlet", Scarlet.class.getMethod("scarlet")));

        EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);
        assembler.createApplication(ejbJarInfo);

        InitialContext context = new InitialContext();

        {
            ColorLocal color = (ColorLocal) context.lookup("ColorLocal");

            assertEquals("Never", color.color());
            assertEquals("RequiresNew", color.color((Object)null));
            assertEquals("Supports", color.color((String)null));
            assertEquals("Supports", color.color((Boolean)null));
            assertEquals("Supports", color.color((Integer)null));
        }

        {
            ColorRemote color = (ColorRemote) context.lookup("ColorRemote");

            assertEquals("Never", color.color());
            assertEquals("RequiresNew", color.color((Object)null));
            assertEquals("Supports", color.color((String)null));
            assertEquals("Supports", color.color((Boolean)null));
            assertEquals("Supports", color.color((Integer)null));
        }

        {
            ColorEjbLocalHome home = (ColorEjbLocalHome) context.lookup("ColorLocalHome");
            ColorEjbLocalObject color = home.create("Supports");

            assertEquals("Never", color.color());
            assertEquals("RequiresNew", color.color((Object)null));
            assertEquals("Supports", color.color((String)null));
            assertEquals("Supports", color.color((Boolean)null));
            assertEquals("Supports", color.color((Integer)null));

            expected.set("Supports");
            color.remove();
        }

        {
            ColorEjbHome home = (ColorEjbHome) context.lookup("ColorRemoteHome");
            ColorEjbObject color = home.create("Supports");

            assertEquals("Never", color.color());
            assertEquals("RequiresNew", color.color((Object)null));
            assertEquals("Supports", color.color((String)null));
            assertEquals("Supports", color.color((Boolean)null));
            assertEquals("Supports", color.color((Integer)null));

            expected.set("Supports");
            color.remove();
        }


        {
            RedLocal red = (RedLocal) context.lookup("RedLocal");
            assertEquals("Never", red.color());
            assertEquals("Required", red.color((Object)null));
            assertEquals("Supports", red.color((String)null));
            assertEquals("Supports", red.color((Boolean)null));
            assertEquals("Supports", red.color((Integer)null));
            assertEquals("RequiresNew", red.red());
            assertEquals("Required", red.red((Object)null));
            assertEquals("Required", red.red((String)null));
        }

        {
            RedRemote red = (RedRemote) context.lookup("RedRemote");
            assertEquals("Never", red.color());
            assertEquals("Required", red.color((Object)null));
            assertEquals("Supports", red.color((String)null));
            assertEquals("Supports", red.color((Boolean)null));
            assertEquals("Supports", red.color((Integer)null));
            assertEquals("RequiresNew", red.red());
            assertEquals("Required", red.red((Object)null));
            assertEquals("Required", red.red((String)null));
        }

        {
            RedEjbLocalHome home = (RedEjbLocalHome) context.lookup("RedLocalHome");
            RedEjbLocalObject red = home.create("Supports");
            assertEquals("Never", red.color());
            assertEquals("Required", red.color((Object)null));
            assertEquals("Supports", red.color((String)null));
            assertEquals("Supports", red.color((Boolean)null));
            assertEquals("Supports", red.color((Integer)null));
            assertEquals("RequiresNew", red.red());
            assertEquals("Required", red.red((Object)null));
            assertEquals("Required", red.red((String)null));

            expected.set("Supports");
            red.remove();
        }

        {
            RedEjbHome home = (RedEjbHome) context.lookup("RedRemoteHome");
            RedEjbObject red = home.create("Supports");
            assertEquals("Never", red.color());
            assertEquals("Required", red.color((Object)null));
            assertEquals("Supports", red.color((String)null));
            assertEquals("Supports", red.color((Boolean)null));
            assertEquals("Supports", red.color((Integer)null));
            assertEquals("RequiresNew", red.red());
            assertEquals("Required", red.red((Object)null));
            assertEquals("Required", red.red((String)null));

            expected.set("Supports");
            red.remove();
        }

        {
            CrimsonLocal crimson = (CrimsonLocal) context.lookup("CrimsonLocal");
            assertEquals("Required", crimson.color());
            assertEquals("Required", crimson.color((Object)null));
            assertEquals("Required", crimson.color((String)null));
            assertEquals("Required", crimson.color((Boolean)null));
            assertEquals("Required", crimson.color((Integer)null));
            assertEquals("RequiresNew", crimson.red());
            assertEquals("Required", crimson.red((Object)null));
            assertEquals("Required", crimson.red((String)null));
            assertEquals("RequiresNew", crimson.crimson());
            assertEquals("Required", crimson.crimson((String)null));
        }

        {
            CrimsonRemote crimson = (CrimsonRemote) context.lookup("CrimsonRemote");
            assertEquals("Required", crimson.color());
            assertEquals("Required", crimson.color((Object)null));
            assertEquals("Required", crimson.color((String)null));
            assertEquals("Required", crimson.color((Boolean)null));
            assertEquals("Required", crimson.color((Integer)null));
            assertEquals("RequiresNew", crimson.red());
            assertEquals("Required", crimson.red((Object)null));
            assertEquals("Required", crimson.red((String)null));
            assertEquals("RequiresNew", crimson.crimson());
            assertEquals("Required", crimson.crimson((String)null));
        }

        {
            CrimsonEjbLocalHome home = (CrimsonEjbLocalHome) context.lookup("CrimsonLocalHome");
            CrimsonEjbLocalObject crimson = home.create("RequiresNew");
            assertEquals("Required", crimson.color());
            assertEquals("Required", crimson.color((Object)null));
            assertEquals("Required", crimson.color((String)null));
            assertEquals("Required", crimson.color((Boolean)null));
            assertEquals("Required", crimson.color((Integer)null));
            assertEquals("RequiresNew", crimson.red());
            assertEquals("Required", crimson.red((Object)null));
            assertEquals("Required", crimson.red((String)null));
            assertEquals("RequiresNew", crimson.crimson());
            assertEquals("Required", crimson.crimson((String)null));
            expected.set("RequiresNew");
            crimson.remove();
        }

        {
            CrimsonEjbHome home = (CrimsonEjbHome) context.lookup("CrimsonRemoteHome");
            CrimsonEjbObject crimson = home.create("Supports");
            assertEquals("Required", crimson.color());
            assertEquals("Required", crimson.color((Object)null));
            assertEquals("Required", crimson.color((String)null));
            assertEquals("Required", crimson.color((Boolean)null));
            assertEquals("Required", crimson.color((Integer)null));
            assertEquals("RequiresNew", crimson.red());
            assertEquals("Required", crimson.red((Object)null));
            assertEquals("Required", crimson.red((String)null));
            assertEquals("RequiresNew", crimson.crimson());
            assertEquals("Required", crimson.crimson((String)null));
            expected.set("RequiresNew");
            crimson.remove();
        }

        {
            ScarletLocal scarlet = (ScarletLocal) context.lookup("ScarletLocal");
            assertEquals("Never", scarlet.color());
            assertEquals("Required", scarlet.color((Object)null));
            assertEquals("RequiresNew", scarlet.color((String)null));
            assertEquals("RequiresNew", scarlet.color((Boolean)null));
            assertEquals("RequiresNew", scarlet.color((Integer)null));
            assertEquals("RequiresNew", scarlet.red());
            assertEquals("Never", scarlet.red((Object)null));
            assertEquals("Never", scarlet.red((String)null));
            assertEquals("Required", scarlet.scarlet());
            assertEquals("NotSupported", scarlet.scarlet((String)null));
        }

        {
            ScarletRemote scarlet = (ScarletRemote) context.lookup("ScarletRemote");
            assertEquals("Never", scarlet.color());
            assertEquals("Required", scarlet.color((Object)null));
            assertEquals("RequiresNew", scarlet.color((String)null));
            assertEquals("RequiresNew", scarlet.color((Boolean)null));
            assertEquals("RequiresNew", scarlet.color((Integer)null));
            assertEquals("RequiresNew", scarlet.red());
            assertEquals("Never", scarlet.red((Object)null));
            assertEquals("Never", scarlet.red((String)null));
            assertEquals("Required", scarlet.scarlet());
            assertEquals("NotSupported", scarlet.scarlet((String)null));
        }

        {
            ScarletEjbLocalHome home = (ScarletEjbLocalHome) context.lookup("ScarletLocalHome");
            ScarletEjbLocalObject scarlet = home.create("RequiresNew");
            assertEquals("Never", scarlet.color());
            assertEquals("Required", scarlet.color((Object)null));
            assertEquals("RequiresNew", scarlet.color((String)null));
            assertEquals("RequiresNew", scarlet.color((Boolean)null));
            assertEquals("RequiresNew", scarlet.color((Integer)null));
            assertEquals("RequiresNew", scarlet.red());
            assertEquals("Never", scarlet.red((Object)null));
            assertEquals("Never", scarlet.red((String)null));
            assertEquals("Required", scarlet.scarlet());
            assertEquals("NotSupported", scarlet.scarlet((String)null));
        }

        {
            ScarletEjbHome home = (ScarletEjbHome) context.lookup("ScarletRemoteHome");
            ScarletEjbObject scarlet = home.create("RequiresNew");
            assertEquals("Never", scarlet.color());
            assertEquals("Required", scarlet.color((Object)null));
            assertEquals("RequiresNew", scarlet.color((String)null));
            assertEquals("RequiresNew", scarlet.color((Boolean)null));
            assertEquals("RequiresNew", scarlet.color((Integer)null));
            assertEquals("RequiresNew", scarlet.red());
            assertEquals("Never", scarlet.red((Object)null));
            assertEquals("Never", scarlet.red((String)null));
            assertEquals("Required", scarlet.scarlet());
            assertEquals("NotSupported", scarlet.scarlet((String)null));
        }

    }

    public static ThreadLocal<String> expected = new ThreadLocal<String>();

    @LocalHome(ColorEjbLocalHome.class)
    @RemoteHome(ColorEjbHome.class)
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public static class Color implements ColorLocal, ColorRemote {

        public String attribute() {
            ThreadContext context = ThreadContext.getThreadContext();
            return context.get(TransactionPolicy.Type.class).toString();
        }

        @Init
        public void ejbCreate(String s){
            assertEquals(s, attribute());
        }

        @Remove
        public void ejbRemove(){
            assertEquals(expected.get(), attribute());
        }


        @TransactionAttribute(TransactionAttributeType.NEVER)
        public String color() {
            return attribute();
        }


        @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
        public String color(Object o) {
            return attribute();
        }

        public String color(String s) {
            return attribute();
        }

        public String color(Boolean b) {
            return attribute();
        }

        public String color(Integer i) {
            return attribute();
        }
    }

    @LocalHome(RedEjbLocalHome.class)
    @RemoteHome(RedEjbHome.class)
    public static class Red extends Color implements RedLocal, RedRemote {

        public String color(Object o) {
            return attribute();
        }

        @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
        public String red() {
            return attribute();
        }

        public String red(Object o) {
            return attribute();
        }

        public String red(String s) {
            return attribute();
        }

    }

    @LocalHome(CrimsonEjbLocalHome.class)
    @RemoteHome(CrimsonEjbHome.class)
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public static class Crimson extends Red implements CrimsonLocal, CrimsonRemote {


        public String color() {
            return attribute();
        }

        public String color(String s) {
            return attribute();
        }

        @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
        public String crimson() {
            return attribute();
        }

        public String crimson(String s) {
            return attribute();
        }
    }

    @LocalHome(ScarletEjbLocalHome.class)
    @RemoteHome(ScarletEjbHome.class)
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public static class Scarlet extends Red implements ScarletLocal, ScarletRemote {

        @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
        public String scarlet() {
            return attribute();
        }

        public String scarlet(String s) {
            return attribute();
        }
    }



    @Local
    public static interface ColorLocal {
        public String color();

        public String color(Object o);

        public String color(String s);

        public String color(Boolean b);

        public String color(Integer i);
    }

    @Remote
    public static interface ColorRemote {
        public String color();

        public String color(Object o);

        public String color(String s);

        public String color(Boolean b);

        public String color(Integer i);
    }

    public static interface ColorEjbHome extends EJBHome {
        ColorEjbObject create(String s) throws CreateException, RemoteException;
    }

    public static interface ColorEjbObject extends EJBObject {
        public String color() throws RemoteException;

        public String color(Object o) throws RemoteException;

        public String color(String s) throws RemoteException;

        public String color(Boolean b) throws RemoteException;

        public String color(Integer i) throws RemoteException;
    }

    public static interface ColorEjbLocalHome extends EJBLocalHome {
        ColorEjbLocalObject create(String s) throws CreateException;
    }

    public static interface ColorEjbLocalObject extends EJBLocalObject {
        public String color();

        public String color(Object o);

        public String color(String s);

        public String color(Boolean b);

        public String color(Integer i);
    }

    @Local
    public static interface RedLocal extends ColorLocal {
        public String red();

        public String red(Object o);

        public String red(String s);
    }

    @Remote
    public static interface RedRemote extends ColorRemote {
        public String red();

        public String red(Object o);

        public String red(String s);
    }

    public static interface RedEjbHome extends ColorEjbHome {
        RedEjbObject create(String s) throws CreateException, RemoteException;
    }

    public static interface RedEjbObject extends ColorEjbObject {
        public String red() throws RemoteException;

        public String red(Object o) throws RemoteException;

        public String red(String s) throws RemoteException;
    }

    public static interface RedEjbLocalHome extends ColorEjbLocalHome {
        RedEjbLocalObject create(String s) throws CreateException;
    }

    public static interface RedEjbLocalObject extends ColorEjbLocalObject {
        public String red();

        public String red(Object o);

        public String red(String s);
    }

    @Local
    public static interface CrimsonLocal extends RedLocal {
        public String crimson();
        public String crimson(String s);
    }

    @Remote
    public static interface CrimsonRemote extends RedRemote {
        public String crimson();
        public String crimson(String s);
    }

    public static interface CrimsonEjbHome extends RedEjbHome {
        CrimsonEjbObject create(String s) throws CreateException, RemoteException;
    }

    public static interface CrimsonEjbObject extends RedEjbObject {
        public String crimson() throws RemoteException;
        public String crimson(String s) throws RemoteException;
    }

    public static interface CrimsonEjbLocalHome extends RedEjbLocalHome {
        CrimsonEjbLocalObject create(String s) throws CreateException;
    }

    public static interface CrimsonEjbLocalObject extends RedEjbLocalObject {
        public String crimson();
        public String crimson(String s);
    }

    @Local
    public static interface ScarletLocal extends RedLocal {
        public String scarlet();
        public String scarlet(String s);
    }

    @Remote
    public static interface ScarletRemote extends RedRemote {
        public String scarlet();
        public String scarlet(String s);
    }

    public static interface ScarletEjbHome extends RedEjbHome {
        ScarletEjbObject create(String s) throws CreateException, RemoteException;
    }

    public static interface ScarletEjbObject extends RedEjbObject {
        public String scarlet() throws RemoteException;
        public String scarlet(String s) throws RemoteException;
    }

    public static interface ScarletEjbLocalHome extends RedEjbLocalHome {
        ScarletEjbLocalObject create(String s) throws CreateException;
    }

    public static interface ScarletEjbLocalObject extends RedEjbLocalObject {
        public String scarlet();
        public String scarlet(String s);
    }

}
