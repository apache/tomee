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
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.Arrays;

/**
 * @version $Rev$ $Date$
 */
public class EjbRefTest extends TestCase {
    private InitialContext context;
    private Assembler assembler;
    private ConfigurationFactory config;

    public void asList(Object... objects){
        Arrays.asList(objects);
    }

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
        for (AppInfo appInfo : assembler.getDeployedApplications()) {
            assembler.destroyApplication(appInfo.jarPath);
        }
        SystemInstance.get().setComponent(Assembler.class, null);
        SystemInstance.get().setComponent(ContainerSystem.class, null);
        super.tearDown();
    }

    public void testInterfaceOnlyRefs() throws Exception {
        ear(ejbjar(Apple.class, AmbiguousFruitRef.class));

        Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        FruitRef fruitRef = get(AmbiguousFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), apple);
    }

    public void testBeanNameRef() throws Exception {
        ear(ejbjar(Apple.class, OrangeFruitRef.class, Orange.class));

        Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        FruitRef fruitRef = get(OrangeFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), orange);
    }

    public void testMappedNameRef() throws Exception {
        ear(ejbjar(Apple.class, AppleFruitRef.class, Orange.class));

        Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        FruitRef fruitRef = get(AppleFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), apple);
    }

    public void testIntraEarInterfaceRef1() throws Exception {
        ear(ejbjar(Apple.class), ejbjar(AmbiguousFruitRef.class));

        Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        FruitRef fruitRef = get(AmbiguousFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), apple);
    }

    public void testIntraEarInterfaceRef() throws Exception {
        ear(ejbjar(Apple.class, AmbiguousFruitRef.class), ejbjar(Orange.class));

        Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        FruitRef fruitRef = get(AmbiguousFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), apple);
    }

    public void testIntraEarBeanNameRef1() throws Exception {
        ear(ejbjar(Apple.class), ejbjar(OrangeFruitRef.class), ejbjar(Orange.class));

        Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        FruitRef fruitRef = get(OrangeFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), orange);
    }

    public void testIntraEarBeanNameRef2() throws Exception {
        ear(ejbjar(Apple.class, OrangeFruitRef.class), ejbjar(Orange.class));

        Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        FruitRef fruitRef = get(OrangeFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), orange);
    }

    public void testInterEarInterfaceRef1() throws Exception {
        ear(ejbjar(Apple.class));
        ear(ejbjar(AmbiguousFruitRef.class));

        Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        FruitRef fruitRef = get(AmbiguousFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), apple);
    }

    public void testInterEarInterfaceRef2() throws Exception {
        ear(ejbjar(Apple.class), ejbjar(AmbiguousFruitRef.class));
        ear(ejbjar(Orange.class));

        Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        FruitRef fruitRef = get(AmbiguousFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), apple);
    }

    public void testInterEarBeanNameRef1() throws Exception {
        ear(ejbjar(Orange.class));
        ear(ejbjar(Apple.class, OrangeFruitRef.class));

        Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        FruitRef fruitRef = get(OrangeFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), orange);
    }

    public void testInterEarBeanNameRef2() throws Exception {
        ear(ejbjar(Orange.class));
        ear(ejbjar(Apple.class));
        ear(ejbjar(OrangeFruitRef.class));

        Fruit apple = get(Apple.class, Fruit.class);
        assertNotNull(apple);

        Fruit orange = get(Orange.class, Fruit.class);
        assertNotNull(orange);

        FruitRef fruitRef = get(OrangeFruitRef.class, FruitRef.class);
        assertNotNull(fruitRef);

        assertEquals(fruitRef.getFruit(), orange);
    }


    public void ear(Class ... beans) throws Exception {
        EjbJar ejbJar = ejbjar(beans);
        ear(ejbJar);
    }

    private void ear(EjbJar... ejbJars) throws OpenEJBException, NamingException, IOException {
        AppModule app = new AppModule(this.getClass().getClassLoader(), "classpath-"+ejbJars.hashCode());
        for (EjbJar ejbJar : ejbJars) {
            app.getEjbModules().add(new EjbModule(ejbJar));
        }
        assembler.createApplication(config.configureApplication(app));
    }

    private EjbJar ejbjar(Class... beans) {
        EjbJar ejbJar = new EjbJar();
        for (Class bean : beans) {
            ejbJar.addEnterpriseBean(new StatelessBean(bean));
        }
        return ejbJar;
    }

    public <T> T get(Class bean, Class<T> intrface) {
        try {
            return (T) context.lookup(bean.getSimpleName() + "Local");
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * A potential issue with this feature is that the stateless bean is created
     * and the object it references does not exist at the time it is instantiated
     * and put into the pool.  Later the bean is deployed yet the instances in the
     * pool remain with null references, only new instances will be able to reference
     * the newly deployed bean.
     *
     * @throws Exception
     */
    public void _test() throws Exception {

        // Inter ear (between ear) Uni-directional references, local interfaces
        EjbJar circleApp = new EjbJar();
        circleApp.addEnterpriseBean(new StatelessBean(CircleBean.class));
        assembler.createApplication(config.configureApplication(circleApp));

        EjbJar squareApp = new EjbJar();
        squareApp.addEnterpriseBean(new StatelessBean(SquareBean.class));
        assembler.createApplication(config.configureApplication(squareApp));


        Square square = (Square) context.lookup("SquareBeanLocal");
        Circle circle = (Circle) context.lookup("CircleBeanLocal");

        assertNotNull(square);
        assertNotNull(circle);

        assertNotNull(square.getCircle());
        assertEquals(circle, square.getCircle());

        // Inter ear (between ear) Bi-directional (circular) references, local interfaces
        EjbJar redApp = new EjbJar();
        redApp.addEnterpriseBean(new StatelessBean(RedBean.class));
        assembler.createApplication(config.configureApplication(redApp));

        EjbJar blackApp = new EjbJar();
        blackApp.addEnterpriseBean(new StatelessBean(RedBean.class));
        assembler.createApplication(config.configureApplication(blackApp));


        Red red = (Red) context.lookup("RedBeanLocal");
        Black black = (Black) context.lookup("BlackBeanLocal");

        assertNotNull(red);
        assertNotNull(red.getBlack());

        assertNotNull(black);
        assertNotNull(black.getRed());

        assertEquals(red, black.getRed());
        assertEquals(black, red.getBlack());


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
    // Uni-directional relationship
    // ------------------------------------------------------- //

    public static class CircleBean implements Circle {

    }

    public static interface Circle {
    }

    public static class SquareBean implements Square {
        @EJB
        Circle circle;

        public Circle getCircle() {
            return circle;
        }
    }

    public static interface Square {
        public Circle getCircle();
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
}
