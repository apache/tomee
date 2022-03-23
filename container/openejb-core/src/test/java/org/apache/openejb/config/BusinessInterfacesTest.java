/**
 * Licensed to the Apache Software Foundation (ASF) under Local or more
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
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.config.rules.ValidationAssertions;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.oejb3.OpenejbJar;

import jakarta.ejb.Local;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Remote;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusinessInterfacesTest extends TestCase {

    private ConfigurationFactory config;
    private EjbModule ejbModule;
    private EjbJar ejbJar;

    @Override
    protected void setUp() throws Exception {
        final Assembler assembler = new Assembler();
        config = new ConfigurationFactory();

        ejbModule = new EjbModule(new EjbJar());
        ejbModule.setOpenejbJar(new OpenejbJar());
        ejbJar = ejbModule.getEjbJar();

        strict(false);
    }

    private void strict(final boolean b) {
        ejbModule.getOpenejbJar().getProperties().setProperty("openejb.strict.interface.declaration", b + "");
    }

    private Map<String, EnterpriseBeanInfo> deploy(final Class<?>... beans) throws OpenEJBException {
        for (final Class<?> bean : beans) {
            addBean(bean);
        }
        final EjbJarInfo ejbJarInfo = config.configureApplication(ejbModule);

        return asMap(ejbJarInfo.enterpriseBeans);
    }

    private StatelessBean addBean(final Class<?> orangeBeanClass) {
        return ejbJar.addEnterpriseBean(new StatelessBean(orangeBeanClass));
    }

    // ----------------------------------------------------------------------------------------------------------------
    //  Yellow
    // ----------------------------------------------------------------------------------------------------------------

    public static interface YellowOneLocal {
    }

    @Local
    public static class YellowOneBean implements YellowOneLocal, Serializable {
    }

    // - - -

    public static interface YellowTwoRemote {
    }

    @Remote
    public static class YellowTwoBean implements YellowTwoRemote, Serializable {
    }

    // - - -

    public static interface YellowThreeUnspecified {
    }

    public static class YellowThreeBean implements YellowThreeUnspecified, Serializable {
    }

    // - - -

    @Local
    public static interface YellowFourLocal {
    }

    public static class YellowFourBean implements YellowFourLocal, Serializable {
    }

    // - - -

    @Remote
    public static interface YellowFiveRemote {
    }

    public static class YellowFiveBean implements YellowFiveRemote, Serializable {
    }


    // - - -

    public static class YellowSixBean implements Serializable {
    }


    public void testYellow() throws Exception {
        // Results should be the same with strict on or off
        for (final boolean strict : Arrays.asList(false, true)) {
            setUp();
            strict(strict);

            final Map<String, EnterpriseBeanInfo> beans = deploy(YellowOneBean.class, YellowTwoBean.class, YellowThreeBean.class, YellowFourBean.class, YellowFiveBean.class, YellowSixBean.class);

            EnterpriseBeanInfo beanInfo;

            beanInfo = beans.get("YellowOneBean");

            assertEquals(list(YellowOneLocal.class), sort(beanInfo.businessLocal));
            assertEquals(list(), sort(beanInfo.businessRemote));
            assertFalse(beanInfo.localbean);

            beanInfo = beans.get("YellowTwoBean");

            assertEquals(list(), sort(beanInfo.businessLocal));
            assertEquals(list(YellowTwoRemote.class), sort(beanInfo.businessRemote));
            assertFalse(beanInfo.localbean);

            beanInfo = beans.get("YellowThreeBean");

            assertEquals(list(YellowThreeUnspecified.class), sort(beanInfo.businessLocal));
            assertEquals(list(), sort(beanInfo.businessRemote));
            assertFalse(beanInfo.localbean);

            beanInfo = beans.get("YellowFourBean");

            assertEquals(list(YellowFourLocal.class), sort(beanInfo.businessLocal));
            assertEquals(list(), sort(beanInfo.businessRemote));
            assertFalse(beanInfo.localbean);

            beanInfo = beans.get("YellowFiveBean");

            assertEquals(list(), sort(beanInfo.businessLocal));
            assertEquals(list(YellowFiveRemote.class), sort(beanInfo.businessRemote));
            assertFalse(beanInfo.localbean);

            beanInfo = beans.get("YellowSixBean");

            assertEquals(list(), sort(beanInfo.businessLocal));
            assertEquals(list(), sort(beanInfo.businessRemote));
            assertTrue(beanInfo.localbean);

        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    //  Lemon -- implied LocalBean and subclassing
    // ----------------------------------------------------------------------------------------------------------------

    public static class LemonOneBean extends YellowOneBean {
    }

    public static class LemonTwoBean extends YellowTwoBean {
    }

    public static class LemonThreeBean extends YellowThreeBean {
    }

    public static class LemonFourBean extends YellowFourBean {
    }

    public static class LemonFiveBean extends YellowFiveBean {
    }

    public static class LemonSixBean extends YellowSixBean {
    }

    public void testLemon() throws Exception {
        setUp();
        strict(false);

        final Map<String, EnterpriseBeanInfo> beans = deploy(LemonOneBean.class, LemonTwoBean.class, LemonThreeBean.class, LemonFourBean.class, LemonFiveBean.class, LemonSixBean.class);

        EnterpriseBeanInfo beanInfo;

        beanInfo = beans.get("LemonOneBean");

        assertEquals(list(YellowOneLocal.class), sort(beanInfo.businessLocal));
        assertEquals(list(), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);

        beanInfo = beans.get("LemonTwoBean");

        assertEquals(list(), sort(beanInfo.businessLocal));
        assertEquals(list(YellowTwoRemote.class), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);

        beanInfo = beans.get("LemonThreeBean");

        assertEquals(list(), sort(beanInfo.businessLocal));
        assertEquals(list(), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);

        beanInfo = beans.get("LemonFourBean");

        assertEquals(list(YellowFourLocal.class), sort(beanInfo.businessLocal));
        assertEquals(list(), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);

        beanInfo = beans.get("LemonFiveBean");

        assertEquals(list(), sort(beanInfo.businessLocal));
        assertEquals(list(YellowFiveRemote.class), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);

        beanInfo = beans.get("LemonSixBean");

        assertEquals(list(), sort(beanInfo.businessLocal));
        assertEquals(list(), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);

    }

    public void testLemonStrict() throws Exception {
        setUp();
        strict(true);

        final Map<String, EnterpriseBeanInfo> beans = deploy(LemonOneBean.class, LemonTwoBean.class, LemonThreeBean.class, LemonFourBean.class, LemonFiveBean.class, LemonSixBean.class);

        EnterpriseBeanInfo beanInfo;

        beanInfo = beans.get("LemonOneBean");

        assertEquals(list(), sort(beanInfo.businessLocal));
        assertEquals(list(), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);

        beanInfo = beans.get("LemonTwoBean");

        assertEquals(list(), sort(beanInfo.businessLocal));
        assertEquals(list(), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);

        beanInfo = beans.get("LemonThreeBean");

        assertEquals(list(), sort(beanInfo.businessLocal));
        assertEquals(list(), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);

        beanInfo = beans.get("LemonFourBean");

        assertEquals(list(), sort(beanInfo.businessLocal));
        assertEquals(list(), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);

        beanInfo = beans.get("LemonFiveBean");

        assertEquals(list(), sort(beanInfo.businessLocal));
        assertEquals(list(), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);

        beanInfo = beans.get("LemonSixBean");

        assertEquals(list(), sort(beanInfo.businessLocal));
        assertEquals(list(), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);

    }

    // ----------------------------------------------------------------------------------------------------------------
    //  Magenta -- explicit @LocalBean
    // ----------------------------------------------------------------------------------------------------------------

    public static interface MagentaOneLocal {
    }

    @Local
    @LocalBean
    public static class MagentaOneBean implements MagentaOneLocal, Serializable {
    }

    // - - -

    public static interface MagentaTwoRemote {
    }

    @Remote
    @LocalBean
    public static class MagentaTwoBean implements MagentaTwoRemote, Serializable {
    }

    // - - -

    public static interface MagentaThreeUnspecified {
    }

    @LocalBean
    public static class MagentaThreeBean implements MagentaThreeUnspecified, Serializable {
    }

    // - - -

    @Local
    public static interface MagentaFourLocal {
    }

    @LocalBean
    public static class MagentaFourBean implements MagentaFourLocal, Serializable {
    }

    // - - -

    @Remote
    public static interface MagentaFiveRemote {
    }

    @LocalBean
    public static class MagentaFiveBean implements MagentaFiveRemote, Serializable {
    }

    public void testMagenta() throws Exception {
        // Results should be the same with strict on or off
        for (final boolean strict : Arrays.asList(false, true)) {
            setUp();
            strict(strict);

            final Map<String, EnterpriseBeanInfo> beans = deploy(MagentaOneBean.class, MagentaTwoBean.class, MagentaThreeBean.class, MagentaFourBean.class, MagentaFiveBean.class);

            EnterpriseBeanInfo beanInfo;

            beanInfo = beans.get("MagentaOneBean");

            assertEquals(list(MagentaOneLocal.class), sort(beanInfo.businessLocal));
            assertEquals(list(), sort(beanInfo.businessRemote));
            assertTrue(beanInfo.localbean);

            beanInfo = beans.get("MagentaTwoBean");

            assertEquals(list(), sort(beanInfo.businessLocal));
            assertEquals(list(MagentaTwoRemote.class), sort(beanInfo.businessRemote));
            assertTrue(beanInfo.localbean);

            beanInfo = beans.get("MagentaThreeBean");

            assertEquals(list(), sort(beanInfo.businessLocal));
            assertEquals(list(), sort(beanInfo.businessRemote));
            assertTrue(beanInfo.localbean);

            beanInfo = beans.get("MagentaFourBean");

            assertEquals(list(MagentaFourLocal.class), sort(beanInfo.businessLocal));
            assertEquals(list(), sort(beanInfo.businessRemote));
            assertTrue(beanInfo.localbean);

            beanInfo = beans.get("MagentaFiveBean");

            assertEquals(list(), sort(beanInfo.businessLocal));
            assertEquals(list(MagentaFiveRemote.class), sort(beanInfo.businessRemote));
            assertTrue(beanInfo.localbean);

        }
    }


    // ----------------------------------------------------------------------------------------------------------------
    //  InvalidYellow
    // ----------------------------------------------------------------------------------------------------------------

    public static interface InvalidYellowOneLocal {
    }

    public static interface InvalidYellowOneLocal2 {
    }

    @Local
    public static class InvalidYellowOneBean implements InvalidYellowOneLocal, InvalidYellowOneLocal2, Serializable {
    }

    // - - -

    public static interface InvalidYellowTwoRemote {
    }

    public static interface InvalidYellowTwoRemote2 {
    }

    @Remote
    public static class InvalidYellowTwoBean implements InvalidYellowTwoRemote, InvalidYellowTwoRemote2, Serializable {
    }

    // - - -

    public static interface InvalidYellowThreeUnspecified {
    }

    public static interface InvalidYellowThreeUnspecified2 {
    }

    public static class InvalidYellowThreeBean implements InvalidYellowThreeUnspecified, InvalidYellowThreeUnspecified2, Serializable {
    }

    //TODO this test fails becuse the error message formatting support has an issue with escaping like \{
    // we really need to fix that
    public void _testInvalidYellow() throws Exception {
        // Results should be the same with strict on or off
        for (final boolean strict : Arrays.asList(false, true)) {
            setUp();
            strict(strict);

            final List<String> expectedKeys = new ArrayList<>();
            expectedKeys.add("ann.local.noAttributes");
            expectedKeys.add("ann.remote.noAttributes");
            expectedKeys.add("noInterfaceDeclared.session");
            expectedKeys.add("noInterfaceDeclared.session");

            try {
                deploy(InvalidYellowOneBean.class, InvalidYellowTwoBean.class, InvalidYellowThreeBean.class);
            } catch (final ValidationFailedException e) {
                ValidationAssertions.assertFailures(expectedKeys, e);
            }
        }
    }


    // ----------------------------------------------------------------------------------------------------------------
    //  Green
    // ----------------------------------------------------------------------------------------------------------------

    public static interface GreenOneLocal {
    }


    public static interface GreenOneRemote {
    }

    @Local
    @Remote(GreenOneRemote.class)
    public static class GreenOneBean implements GreenOneLocal, GreenOneRemote {
    }

    // - - -

    public static interface GreenTwoLocal {
    }


    public static interface GreenTwoRemote {
    }

    @Local(GreenTwoLocal.class)
    @Remote
    public static class GreenTwoBean implements GreenTwoLocal, GreenTwoRemote {
    }

    public void testGreen() throws Exception {
        // Results should be the same with strict on or off
        for (final boolean strict : Arrays.asList(false, true)) {
            setUp();
            strict(strict);

            final Map<String, EnterpriseBeanInfo> beans = deploy(GreenOneBean.class, GreenTwoBean.class);

            EnterpriseBeanInfo beanInfo;

            beanInfo = beans.get("GreenOneBean");

            assertEquals(list(GreenOneLocal.class), sort(beanInfo.businessLocal));
            assertEquals(list(GreenOneRemote.class), sort(beanInfo.businessRemote));

            beanInfo = beans.get("GreenTwoBean");

            assertEquals(list(GreenTwoLocal.class), sort(beanInfo.businessLocal));
            assertEquals(list(GreenTwoRemote.class), sort(beanInfo.businessRemote));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    //  Orange
    // ----------------------------------------------------------------------------------------------------------------

    @Local
    public static interface OrangeOneLocal {
    }

    @Remote
    public static interface OrangeOneRemote {
    }

    @Local
    @Remote
    public static interface OrangeOneBoth {
    }

    public static interface OrangeOneUnspecified {
    }

    public static class OrangeOneBean implements OrangeOneLocal, OrangeOneRemote, OrangeOneBoth, OrangeOneUnspecified {
    }

    // - - -

    public static interface OrangeTwoLocal {
    }

    public static interface OrangeTwoRemote {
    }

    public static interface OrangeTwoBoth {
    }

    public static interface OrangeTwoUnspecified {
    }

    @Local({OrangeTwoLocal.class, OrangeTwoBoth.class})
    @Remote({OrangeTwoRemote.class, OrangeTwoBoth.class})
    public static class OrangeTwoBean implements OrangeTwoLocal, OrangeTwoRemote, OrangeTwoBoth, OrangeTwoUnspecified {
    }

    public void testOrangeNotStrict() throws Exception {
        strict(false);

        final Map<String, EnterpriseBeanInfo> beans = deploy(OrangeOneBean.class, OrangeTwoBean.class);

        EnterpriseBeanInfo beanInfo = beans.get("OrangeOneBean");

        assertEquals(list(OrangeOneLocal.class, OrangeOneBoth.class), sort(beanInfo.businessLocal));
        assertEquals(list(OrangeOneRemote.class, OrangeOneBoth.class), sort(beanInfo.businessRemote));

        beanInfo = beans.get("OrangeTwoBean");

        assertEquals(list(OrangeTwoLocal.class, OrangeTwoBoth.class), sort(beanInfo.businessLocal));
        assertEquals(list(OrangeTwoRemote.class, OrangeTwoBoth.class), sort(beanInfo.businessRemote));
    }

    public void testOrangeStrict() throws Exception {
        strict(true);

        final List<String> expectedKeys = new ArrayList<>();
        expectedKeys.add("ann.localRemote.conflict");
        expectedKeys.add("ann.localRemote.conflict");

        try {
            deploy(OrangeOneBean.class, OrangeTwoBean.class);
        } catch (final ValidationFailedException e) {
            ValidationAssertions.assertFailures(expectedKeys, e);
        }
    }


    // ----------------------------------------------------------------------------------------------------------------
    //  Red
    // ----------------------------------------------------------------------------------------------------------------

    @Local
    public static interface RedOneLocal {
    }

    public static interface RedOneRemote {
    }

    @Local
    public static interface RedOneOverridden {
    }

    public static interface RedOneUnspecified {
    }

    @Remote({RedOneRemote.class, RedOneOverridden.class})
    public static class RedOneBean implements RedOneLocal, RedOneRemote, RedOneOverridden, RedOneUnspecified {
    }

    // - - -

    public static interface RedTwoLocal {
    }

    @Remote
    public static interface RedTwoRemote {
    }

    @Remote
    public static interface RedTwoOverridden {
    }

    public static interface RedTwoUnspecified {
    }

    @Local({RedTwoLocal.class, RedTwoOverridden.class})
    public static class RedTwoBean implements RedTwoLocal, RedTwoRemote, RedTwoOverridden, RedTwoUnspecified {
    }

    /**
     * Definition in the bean class wins over the
     *
     * @throws Exception
     */
    public void testRedNotStrict() throws Exception {
        strict(false);

        final Map<String, EnterpriseBeanInfo> beans = deploy(RedOneBean.class, RedTwoBean.class);

        EnterpriseBeanInfo beanInfo = beans.get("RedOneBean");

        assertEquals(list(RedOneLocal.class), sort(beanInfo.businessLocal));
        assertEquals(list(RedOneRemote.class, RedOneOverridden.class), sort(beanInfo.businessRemote));

        beanInfo = beans.get("RedTwoBean");

        assertEquals(list(RedTwoLocal.class, RedTwoOverridden.class), sort(beanInfo.businessLocal));
        assertEquals(list(RedTwoRemote.class), sort(beanInfo.businessRemote));
    }

    /**
     * Test results should be the same as above.
     *
     * @throws Exception
     */
    public void testRedStrict() throws Exception {
        strict(true);

        final Map<String, EnterpriseBeanInfo> beans = deploy(RedOneBean.class, RedTwoBean.class);

        EnterpriseBeanInfo beanInfo = beans.get("RedOneBean");

        assertEquals(list(RedOneLocal.class), sort(beanInfo.businessLocal));
        assertEquals(list(RedOneRemote.class, RedOneOverridden.class), sort(beanInfo.businessRemote));

        beanInfo = beans.get("RedTwoBean");

        assertEquals(list(RedTwoLocal.class, RedTwoOverridden.class), sort(beanInfo.businessLocal));
        assertEquals(list(RedTwoRemote.class), sort(beanInfo.businessRemote));
    }

    public static class CrimsonOneBean extends RedOneBean {
    }

    public static class CrimsonTwoBean extends RedTwoBean {
    }


    /**
     * Super class definitions are retrieved
     *
     * @throws Exception
     */
    public void testCrimsonNotStrict() throws Exception {
        strict(false);

        final Map<String, EnterpriseBeanInfo> beans = deploy(CrimsonOneBean.class, CrimsonTwoBean.class);

        EnterpriseBeanInfo beanInfo = beans.get("CrimsonOneBean");

        assertEquals(list(RedOneLocal.class), sort(beanInfo.businessLocal));
        assertEquals(list(RedOneRemote.class, RedOneOverridden.class), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);

        beanInfo = beans.get("CrimsonTwoBean");

        assertEquals(list(RedTwoLocal.class, RedTwoOverridden.class), sort(beanInfo.businessLocal));
        assertEquals(list(RedTwoRemote.class), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);
    }

    /**
     * Test results should NOT be the same as above. Super class should not be consulted
     *
     * @throws Exception
     */
    public void testCrimsonStrict() throws Exception {
        strict(true);

        final Map<String, EnterpriseBeanInfo> beans = deploy(CrimsonOneBean.class, CrimsonTwoBean.class);

        EnterpriseBeanInfo beanInfo = beans.get("CrimsonOneBean");

        assertEquals(list(), sort(beanInfo.businessLocal));
        assertEquals(list(), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);

        beanInfo = beans.get("CrimsonTwoBean");

        assertEquals(list(), sort(beanInfo.businessLocal));
        assertEquals(list(), sort(beanInfo.businessRemote));
        assertTrue(beanInfo.localbean);
    }


    private <T extends Comparable<? super T>> List<T> sort(final List<T> list) {
        Collections.sort(list);
        return list;
    }

    private Map<String, EnterpriseBeanInfo> asMap(final List<EnterpriseBeanInfo> enterpriseBeans) {
        final Map<String, EnterpriseBeanInfo> map = new HashMap<>();
        for (final EnterpriseBeanInfo bean : enterpriseBeans) {
            map.put(bean.ejbName, bean);
        }

        return map;
    }

    private List<String> list(final Class... classes) {
        final ArrayList<String> list = new ArrayList<>();
        for (final Class clazz : classes) {
            list.add(clazz.getName());
        }
        return sort(list);
    }


    // RedBean ----------------------------------------------------------------

    // Interfaces == @Local
    // Bean Class == @Remote

    // Opposite scenario is GreenBean

    @Local
    public static interface RedLocal {
    }

    public static interface RedRemote {
    }

    @Local
    public static interface RedBoth {
    }

    public static interface RedUnspecified {
    }

    @Remote({RedRemote.class, RedBoth.class})
    public static class RedBean implements RedLocal, RedRemote, RedBoth, RedUnspecified {
    }

    // GreenBean ----------------------------------------------------------------

    // Interfaces == @Remote
    // Bean Class == @Local

    // Green is opposite of Red

    public static interface GreenLocal {
    }

    @Remote
    public static interface GreenRemote {
    }

    @Remote
    public static interface GreenBoth {
    }

    public static interface GreenUnspecified {
    }

    @Local({GreenLocal.class, GreenBoth.class})
    public static class GreenBean implements GreenLocal, GreenRemote, GreenBoth, GreenUnspecified {
    }

    // YellowBean ----------------------------------------------------------------

    // Interfaces == @Local
    // Bean Class == @Remote

    // Opposite scenario is PurpleBean

    public static interface YellowLocal {
    }

    public static interface YellowRemote {
    }

    @Local
    @Remote
    public static interface YellowBoth {
    }

    public static interface YellowUnspecified {
    }

    @Local(YellowLocal.class)
    @Remote(YellowRemote.class)
    public static class YellowBean implements YellowLocal, YellowRemote, YellowBoth, YellowUnspecified {
    }

    // PurpleBean ----------------------------------------------------------------

    // Interfaces == @Remote
    // Bean Class == @Local

    // Purple is opposite of Yellow

    @Local
    public static interface PurpleLocal {
    }

    @Remote
    public static interface PurpleRemote {
    }

    public static interface PurpleBoth {
    }

    public static interface PurpleUnspecified {
    }

    @Local({PurpleBoth.class})
    @Remote({PurpleBoth.class})
    public static class PurpleBean implements PurpleLocal, PurpleRemote, PurpleBoth, PurpleUnspecified {
    }

    // WhiteBean ----------------------------------------------------------------

    // Not annotated -- xml only

    public static interface WhiteLocal {
    }

    public static interface WhiteRemote {
    }

    public static interface WhiteBoth {
    }

    public static interface WhiteUnspecified {
    }

    public static class WhiteBean implements WhiteLocal, WhiteRemote, WhiteBoth, WhiteUnspecified {
    }

    // LightOrangeBean ----------------------------------------------------------------

    // local == xml
    // remote == annotations on interfaces

    public static interface LightOrangeLocal {
    }

    @Remote
    public static interface LightOrangeRemote {
    }

    @Remote
    public static interface LightOrangeBoth {
    }

    public static interface LightOrangeUnspecified {
    }

    public static class LightOrangeBean implements LightOrangeLocal, LightOrangeRemote, LightOrangeBoth, LightOrangeUnspecified {
    }

    // LightBlueBean ----------------------------------------------------------------

    // local == annotations on interfaces
    // remote == xml

    @Local
    public static interface LightBlueLocal {
    }

    public static interface LightBlueRemote {
    }

    @Local
    public static interface LightBlueBoth {
    }

    public static interface LightBlueUnspecified {
    }

    public static class LightBlueBean implements LightBlueLocal, LightBlueRemote, LightBlueBoth, LightBlueUnspecified {
    }

    // LightRedBean ----------------------------------------------------------------

    // local == xml
    // remote == annotations on bean class

    public static interface LightRedLocal {
    }

    public static interface LightRedRemote {
    }

    public static interface LightRedBoth {
    }

    public static interface LightRedUnspecified {
    }

    @Remote({LightRedRemote.class, LightRedBoth.class})
    public static class LightRedBean implements LightRedLocal, LightRedRemote, LightRedBoth, LightRedUnspecified {
    }

    // LightGreenBean ----------------------------------------------------------------

    // local == annotations on bean class
    // remote == xml

    public static interface LightGreenLocal {
    }

    public static interface LightGreenRemote {
    }

    public static interface LightGreenBoth {
    }

    public static interface LightGreenUnspecified {
    }

    @Local({LightGreenLocal.class, LightGreenBoth.class})
    public static class LightGreenBean implements LightGreenLocal, LightGreenRemote, LightGreenBoth, LightGreenUnspecified {
    }


    // MagentaBean ----------------------------------------------------------------


    public static interface MagentaLocal {
    }

    public static interface MagentaRemote {
    }

    @Local
    @Remote(MagentaRemote.class)
    public static class MagentaBean implements MagentaLocal {
    }

    // CyanBean ----------------------------------------------------------------

    public static interface CyanLocal {
    }

    public static interface CyanRemote {
    }

    @Local(CyanLocal.class)
    @Remote
    public static class CyanBean implements CyanRemote {
    }

    // PurpleBean ----------------------------------------------------------------

    public static interface Violet {
    }

    @Local
    @Remote
    public static class VioletBean implements Violet {
    }

}