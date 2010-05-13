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
package org.apache.openejb.monitoring;

import org.apache.openejb.api.Monitor;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.core.ivm.naming.InitContextFactory;

import javax.interceptor.InvocationContext;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.lang.reflect.Method;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Properties;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @version $Rev$ $Date$
 */
public class MethodScratchPad {
    public static void main(String[] args) throws Exception {
        Runnable methods  = new Runnable() {
            public void run() {
                try {
                    new MethodScratchPad().main();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable pool = new Runnable() {
            public void run() {
                try {
                    new ScratchPad().main();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(methods).start();
//        new Thread(pool).start();
    }

    public void main() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(MyBean.class));
        assembler.createApplication(config.configureApplication(ejbJar));

        javax.naming.Context context = new InitialContext();
        Object bean = context.lookup("MyBeanLocal");

        List<Method> methods = new ArrayList<Method>();
        Class<?> clazz = MyBeanLocal.class;
        methods.add(clazz.getMethod("red"));
        methods.add(clazz.getMethod("red"));
        methods.add(clazz.getMethod("red"));
        methods.add(clazz.getMethod("red"));
        methods.add(clazz.getMethod("red"));
        methods.add(clazz.getMethod("blue"));
        methods.add(clazz.getMethod("blue"));
        methods.add(clazz.getMethod("blue"));
        methods.add(clazz.getMethod("green"));
        methods.add(clazz.getMethod("green"));
        methods.add(clazz.getMethod("orange"));

        Random random = new Random();

        while (true) {
            methods.get(random(random, 0, methods.size())).invoke(bean);
        }
    }

    @Monitor
    @Stateless
    public static class MyBean implements MyBeanLocal {
        public void red() {
            snooze((int) (System.nanoTime() / 1000 % 287));
        }

        public void green() {
            snooze((int) (System.nanoTime() / 1000 % 375));
        }

        public void blue() {
            snooze((int) (System.nanoTime() / 1000 % 867));
        }

        public void orange() {
            snooze((int) (System.nanoTime() / 1000 % 639));
        }
    }

    public static interface MyBeanLocal {
        public void red();
        public void green();
        public void blue();
        public void orange();
    }

    private static void snooze(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }
    
    public static int random(Random random, int min, int max) {
        int i = random.nextInt();
        if (i < 0) i *= -1;
        return (i % (max - min)) + min;
    }

}
