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
package org.apache.openejb.config;

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.client.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;

import javax.ejb.Local;
import javax.ejb.Remote;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusinessInterfacesTest extends TestCase {

    public void test() throws Exception {
    }

    public void _testDefault() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        SystemInstance.get().setProperty("openejb.strict.interface.declaration", "false");

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(OrangeBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(RedBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(YellowBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(GreenBean.class));

        StatelessBean white = ejbJar.addEnterpriseBean(new StatelessBean(WhiteBean.class));
        white.addBusinessLocal(WhiteOne.class);
        white.addBusinessLocal(WhiteThree.class);

        StatelessBean black = ejbJar.addEnterpriseBean(new StatelessBean(BlackBean.class));
        black.addBusinessRemote(BlackTwo.class);
        black.addBusinessRemote(BlackThree.class);

        ejbJar.addEnterpriseBean(new StatelessBean(MagentaBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(CyanBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(PurpleBean.class));

        EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);

        Map<String, EnterpriseBeanInfo> beans = asMap(ejbJarInfo.enterpriseBeans);

        EnterpriseBeanInfo beanInfo = beans.get("OrangeBean");

        assertEquals(list(OrangeOne.class, OrangeThree.class, OrangeFour.class), sort(beanInfo.businessLocal));
        assertEquals(list(OrangeTwo.class, OrangeThree.class), sort(beanInfo.businessRemote));

        beanInfo = beans.get("RedBean");

        assertEquals(list(RedOne.class, RedThree.class, RedFour.class), sort(beanInfo.businessLocal));
        assertEquals(list(RedTwo.class, RedThree.class), sort(beanInfo.businessRemote));

        beanInfo = beans.get("YellowBean");

        assertEquals(list(YellowOne.class, YellowThree.class, YellowFour.class), sort(beanInfo.businessLocal));
        assertEquals(list(YellowTwo.class, YellowThree.class), sort(beanInfo.businessRemote));

        beanInfo = beans.get("GreenBean");

        assertEquals(list(GreenOne.class, GreenThree.class, GreenFour.class), sort(beanInfo.businessLocal));
        assertEquals(list(GreenTwo.class, GreenThree.class), sort(beanInfo.businessRemote));

        beanInfo = beans.get("WhiteBean");

        assertEquals(list(WhiteOne.class, WhiteThree.class, WhiteFour.class), sort(beanInfo.businessLocal));
        assertEquals(list(WhiteTwo.class, WhiteThree.class), sort(beanInfo.businessRemote));

        beanInfo = beans.get("BlackBean");

        assertEquals(list(BlackOne.class, BlackThree.class, BlackFour.class), sort(beanInfo.businessLocal));
        assertEquals(list(BlackTwo.class, BlackThree.class), sort(beanInfo.businessRemote));

    }

    public void _testStrict() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        SystemInstance.get().setProperty("openejb.strict.interface.declaration", "true");

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(OrangeBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(RedBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(YellowBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(GreenBean.class));

        StatelessBean white = ejbJar.addEnterpriseBean(new StatelessBean(WhiteBean.class));
        white.addBusinessLocal(WhiteOne.class);
        white.addBusinessLocal(WhiteThree.class);

        StatelessBean black = ejbJar.addEnterpriseBean(new StatelessBean(BlackBean.class));
        black.addBusinessRemote(BlackTwo.class);
        black.addBusinessRemote(BlackThree.class);

        EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);

        Map<String, EnterpriseBeanInfo> beans = asMap(ejbJarInfo.enterpriseBeans);

        EnterpriseBeanInfo beanInfo = beans.get("OrangeBean");

        assertEquals(list(OrangeOne.class, OrangeThree.class, OrangeFour.class), sort(beanInfo.businessLocal));
        assertEquals(list(OrangeTwo.class, OrangeThree.class), sort(beanInfo.businessRemote));

        beanInfo = beans.get("RedBean");

        assertEquals(list(RedOne.class, RedThree.class, RedFour.class), sort(beanInfo.businessLocal));
        assertEquals(list(RedTwo.class, RedThree.class), sort(beanInfo.businessRemote));

        beanInfo = beans.get("YellowBean");

        assertEquals(list(YellowOne.class, YellowThree.class, YellowFour.class), sort(beanInfo.businessLocal));
        assertEquals(list(YellowTwo.class, YellowThree.class), sort(beanInfo.businessRemote));

        beanInfo = beans.get("GreenBean");

        assertEquals(list(GreenOne.class, GreenThree.class, GreenFour.class), sort(beanInfo.businessLocal));
        assertEquals(list(GreenTwo.class, GreenThree.class), sort(beanInfo.businessRemote));

        beanInfo = beans.get("WhiteBean");

        assertEquals(list(WhiteOne.class, WhiteThree.class, WhiteFour.class), sort(beanInfo.businessLocal));
        assertEquals(list(WhiteTwo.class, WhiteThree.class), sort(beanInfo.businessRemote));

        beanInfo = beans.get("BlackBean");

        assertEquals(list(BlackOne.class, BlackThree.class, BlackFour.class), sort(beanInfo.businessLocal));
        assertEquals(list(BlackTwo.class, BlackThree.class), sort(beanInfo.businessRemote));

    }

    private <T extends Comparable<? super T>> List<T> sort(List<T> list) {
        Collections.sort(list);
        return list;
    }

    private Map<String, EnterpriseBeanInfo> asMap(List<EnterpriseBeanInfo> enterpriseBeans) {
        Map<String, EnterpriseBeanInfo> map = new HashMap<String, EnterpriseBeanInfo>();
        for (EnterpriseBeanInfo bean : enterpriseBeans) {
            map.put(bean.ejbName, bean);
        }

        return map;
    }

    private List<String> list(Class... classes) {
        ArrayList<String> list = new ArrayList<String>();
        for (Class clazz : classes) {
            list.add(clazz.getName());
        }
        return sort(list);
    }


    // OrangeBean ----------------------------------------------------------------

    @Local
    public static interface OrangeOne {
    }

    @Remote
    public static interface OrangeTwo {
    }

    @Local
    @Remote
    public static interface OrangeThree {
    }

    public static interface OrangeFour {
    }

    public static class OrangeBean implements OrangeOne, OrangeTwo, OrangeThree, OrangeFour {
    }

    // RedBean ----------------------------------------------------------------

    @Local
    public static interface RedOne {
    }

    @Remote
    public static interface RedTwo {
    }

    @Local
    @Remote
    public static interface RedThree {
    }

    public static interface RedFour {
    }

    @Local({RedOne.class, RedThree.class})
    @Remote({RedTwo.class, RedThree.class})
    public static class RedBean implements RedOne, RedTwo, RedThree, RedFour {
    }

    // YellowBean ----------------------------------------------------------------

    @Local
    public static interface YellowOne {
    }

    public static interface YellowTwo {
    }

    @Local
    public static interface YellowThree {
    }

    public static interface YellowFour {
    }

    @Remote({YellowTwo.class, YellowThree.class})
    public static class YellowBean implements YellowOne, YellowTwo, YellowThree, YellowFour {
    }

    // GreenBean ----------------------------------------------------------------

    public static interface GreenOne {
    }

    @Remote
    public static interface GreenTwo {
    }

    @Remote
    public static interface GreenThree {
    }

    public static interface GreenFour {
    }

    @Local({GreenOne.class, GreenThree.class})
    public static class GreenBean implements GreenOne, GreenTwo, GreenThree, GreenFour {
    }

    // WhiteBean ----------------------------------------------------------------

    public static interface WhiteOne {
    }

    @Remote
    public static interface WhiteTwo {
    }

    @Remote
    public static interface WhiteThree {
    }

    public static interface WhiteFour {
    }

    // local interfaces declared in deployment descriptor
    //@Local({WhiteOne.class, WhiteThree.class})
    public static class WhiteBean implements WhiteOne, WhiteTwo, WhiteThree, WhiteFour {
    }

    // BlackBean ----------------------------------------------------------------

    @Local
    public static interface BlackOne {
    }

    public static interface BlackTwo {
    }

    @Local
    public static interface BlackThree {
    }

    public static interface BlackFour {
    }

    // remote interfaces declared in deployment descriptor
    //@Remote({BlackTwo.class, BlackThree.class})
    public static class BlackBean implements BlackOne, BlackTwo, BlackThree, BlackFour {
    }


    // MagentaBean ----------------------------------------------------------------


    public static interface MagentaOne { }
    public static interface MagentaTwo { }

    @Local
    @Remote(MagentaTwo.class)
    public static class MagentaBean implements MagentaOne {
    }

    // CyanBean ----------------------------------------------------------------

    public static interface CyanOne { }
    public static interface CyanTwo { }

    @Local(CyanOne.class)
    @Remote
    public static class CyanBean implements CyanTwo {
    }

    // PurpleBean ----------------------------------------------------------------

    public static interface Purple { }

    @Local
    @Remote
    public static class PurpleBean implements Purple {
    }

}