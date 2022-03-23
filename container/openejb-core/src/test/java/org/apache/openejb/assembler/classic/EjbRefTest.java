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
package org.apache.openejb.assembler.classic;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.AfterClass;

import jakarta.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;

/**
 * A potential issue with this feature is that the stateless bean is created
 * and the object it references does not exist at the time it is instantiated
 * and put into the pool.  Later the bean is deployed yet the instances in the
 * pool remain with null references, only new instances will be able to reference
 * the newly deployed bean.
 *
 * @version $Rev$ $Date$
 */
public class EjbRefTest extends TestCase {
    private InitialContext context;
    private Assembler assembler;
    private ConfigurationFactory config;

    protected void setUp() throws Exception {
        config = new ConfigurationFactory();
        assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        assembler.createContainer(config.configureService(StatelessSessionContainerInfo.class));

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());
        context = new InitialContext();
    }

    protected void tearDown() throws Exception {
        for (final AppInfo appInfo : assembler.getDeployedApplications()) {
            assembler.destroyApplication(appInfo.path);
        }
        SystemInstance.get().setComponent(Assembler.class, null);
        SystemInstance.get().setComponent(ContainerSystem.class, null);
        super.tearDown();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    public void testInterfaceOnlyRefs() throws Exception {
        ear(ejbjar(Apple.class, AmbiguousFruitRef.class));

        final Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        final FruitRef fruitRef = get(AmbiguousFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), apple);
    }

    public void testBeanNameRef() throws Exception {
        ear(ejbjar(Apple.class, OrangeFruitRef.class, Orange.class));

        final Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        final Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        final FruitRef fruitRef = get(OrangeFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), orange);
    }

    public void testMappedNameRef() throws Exception {
        ear(ejbjar(Apple.class, AppleFruitRef.class, Orange.class));

        final Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        final Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        final FruitRef fruitRef = get(AppleFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), apple);
    }

    public void testIntraEarInterfaceRef1() throws Exception {
        ear(ejbjar(Apple.class), ejbjar(AmbiguousFruitRef.class));

        final Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        final FruitRef fruitRef = get(AmbiguousFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), apple);
    }

    public void testIntraEarInterfaceRef() throws Exception {
        ear(ejbjar(Apple.class, AmbiguousFruitRef.class), ejbjar(Orange.class));

        final Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        final Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        final FruitRef fruitRef = get(AmbiguousFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), apple);
    }

    public void testIntraEarBeanNameRef1() throws Exception {
        ear(ejbjar(Apple.class), ejbjar(OrangeFruitRef.class), ejbjar(Orange.class));

        final Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        final Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        final FruitRef fruitRef = get(OrangeFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), orange);
    }

    public void testIntraEarBeanNameRef2() throws Exception {
        ear(ejbjar(Apple.class, OrangeFruitRef.class), ejbjar(Orange.class));

        final Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        final Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        final FruitRef fruitRef = get(OrangeFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), orange);
    }

    public void testInterEarInterfaceRef1() throws Exception {
        ear(ejbjar(Apple.class));
        ear(ejbjar(AmbiguousFruitRef.class));

        final Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        final FruitRef fruitRef = get(AmbiguousFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), apple);
    }

    public void testInterEarInterfaceRef2() throws Exception {
        ear(ejbjar(Apple.class), ejbjar(AmbiguousFruitRef.class));
        ear(ejbjar(Orange.class));

        final Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        final Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        final FruitRef fruitRef = get(AmbiguousFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), apple);
    }

    public void testInterEarBeanNameRef1() throws Exception {
        ear(ejbjar(Orange.class));
        ear(ejbjar(Apple.class, OrangeFruitRef.class));

        final Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        final Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        final FruitRef fruitRef = get(OrangeFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), orange);
    }

    public void testInterEarBeanNameRef2() throws Exception {
        ear(ejbjar(Orange.class));
        ear(ejbjar(Apple.class));
        ear(ejbjar(OrangeFruitRef.class));

        final Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        final Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        final FruitRef fruitRef = get(OrangeFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), orange);
    }

    public void testInterEarLazyInterfaceRef1() throws Exception {
        ear(ejbjar(AmbiguousFruitRef.class));
        ear(ejbjar(Apple.class));

        final Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        final FruitRef fruitRef = get(AmbiguousFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), apple);
    }

    public void testInterEarLazyBeanNameRef1() throws Exception {
        ear(ejbjar(Apple.class, OrangeFruitRef.class));
        ear(ejbjar(Orange.class));

        final Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        final Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        final FruitRef fruitRef = get(OrangeFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), orange);
    }

    public void testInterEarLazyBeanNameRef2() throws Exception {
        ear(ejbjar(OrangeFruitRef.class));
        ear(ejbjar(Apple.class));
        ear(ejbjar(Orange.class));

        final Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        final Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        final FruitRef fruitRef = get(OrangeFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), orange);
    }

    public void testInterEarCircularBeanNameRef() throws Exception {
        ear(ejbjar(BlueBean.class));
        ear(ejbjar(WhiteBean.class));

        final Blue blue = get(BlueBean.class, Blue.class);
        final White white = get(WhiteBean.class, White.class);

        assertNotNull(blue);
        assertNotNull(blue.getWhite());

        assertNotNull(white);
        assertNotNull(white.getBlue());

        assertEquals(blue, white.getBlue());
        assertEquals(white, blue.getWhite());
    }

    public void testInterEarCircularInterfaceRef() throws Exception {
        ear(ejbjar(RedBean.class));
        ear(ejbjar(BlackBean.class));

        final Red red = get(RedBean.class, Red.class);
        final Black black = get(BlackBean.class, Black.class);

        assertNotNull(red);
        assertNotNull(red.getBlack());

        assertNotNull(black);
        assertNotNull(black.getRed());

        assertEquals(red, black.getRed());
        assertEquals(black, red.getBlack());
    }

    public void testSameInterfaceDifferentName() throws Exception {
        ear(ejbjar(Yellow.class, Green.class, YellowGreenBean.class));

        final YellowGreen bean = get(YellowGreenBean.class, YellowGreen.class);

        assertNotNull(bean);
        assertEquals("Yellow", bean.getYellow());
        assertEquals("Green", bean.getGreen());
    }


    public void ear(final Class... beans) throws Exception {
        final EjbJar ejbJar = ejbjar(beans);
        ear(ejbJar);
    }

    private void ear(final EjbJar... ejbJars) throws OpenEJBException, NamingException, IOException {
        final AppModule app = new AppModule(this.getClass().getClassLoader(), "classpath-" + ejbJars.hashCode());
        for (final EjbJar ejbJar : ejbJars) {
            app.getEjbModules().add(new EjbModule(ejbJar));
        }
        assembler.createApplication(config.configureApplication(app));
    }

    private EjbJar ejbjar(final Class... beans) {
        final EjbJar ejbJar = new EjbJar();
        for (final Class bean : beans) {
            ejbJar.addEnterpriseBean(new StatelessBean(bean));
        }
        return ejbJar;
    }

    public <T> T get(final Class bean, final Class<T> intrface) {
        try {
            return (T) context.lookup(bean.getSimpleName() + "Local");
        } catch (final NamingException e) {
            throw new IllegalStateException(e);
        }
    }

    // ------------------------------------------------------- //
    // Ref not unique by interface alone
    // ------------------------------------------------------- //
    public static class Apple implements Fruit {
    }

    public static class Orange implements Fruit {
    }

    public static interface Fruit {
    }

    public static class AmbiguousFruitRef implements FruitRef {
        @EJB
        Fruit fruit;

        public Fruit getFruit() {
            return fruit;
        }
    }

    public static class OrangeFruitRef implements FruitRef {
        @EJB(beanName = "Orange")
        Fruit fruit;

        public Fruit getFruit() {
            return fruit;
        }
    }

    public static class AppleFruitRef implements FruitRef {
        @EJB(mappedName = "Apple")
        Fruit fruit;

        public Fruit getFruit() {
            return fruit;
        }
    }

    public static interface FruitRef {
        Fruit getFruit();
    }

    // ------------------------------------------------------- //
    // bi-directional relationship
    // ------------------------------------------------------- //

    public static class RedBean implements Red {
        @EJB
        Black black;

        public Black getBlack() {
            return black;
        }
    }

    public static interface Red {
        public Black getBlack();
    }

    public static class BlackBean implements Black {
        @EJB
        Red red;

        public Red getRed() {
            return red;
        }
    }

    public static interface Black {
        public Red getRed();
    }

    public static class BlueBean implements Blue {
        @EJB(beanName = "WhiteBean")
        White white;

        public White getWhite() {
            return white;
        }
    }

    public static interface Blue {
        public White getWhite();
    }

    public static class WhiteBean implements White {
        @EJB(beanName = "BlueBean")
        Blue blue;

        public Blue getBlue() {
            return blue;
        }
    }

    public static interface White {
        public Blue getBlue();
    }


    public interface Color {
        String getColor();
    }

    public static class Yellow implements Color {
        public String getColor() {
            return "Yellow";
        }
    }

    public static class Green implements Color {
        public String getColor() {
            return "Green";
        }
    }

    public static class YellowGreenBean implements YellowGreen {
        @EJB
        Color yellow;
        @EJB
        Color green;

        public String getGreen() {
            return green.getColor();
        }

        public String getYellow() {
            return yellow.getColor();
        }
    }

    public static interface YellowGreen {
        String getYellow();

        String getGreen();
    }

}
