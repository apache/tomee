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

import javax.interceptor.InvocationContext;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.reflect.Method;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * @version $Rev$ $Date$
 */
public class MethodScratchPad {
    public static void main(String[] args) throws Exception {
        new MethodScratchPad().main();
    }

    public void main() throws Exception {

        List<Context> contexts = new ArrayList<Context>();
        Class<MyBean> clazz = MyBean.class;
        contexts.add(new Context(clazz.getMethod("red")));
        contexts.add(new Context(clazz.getMethod("red")));
        contexts.add(new Context(clazz.getMethod("red")));
        contexts.add(new Context(clazz.getMethod("red")));
        contexts.add(new Context(clazz.getMethod("red")));
        contexts.add(new Context(clazz.getMethod("blue")));
        contexts.add(new Context(clazz.getMethod("blue")));
        contexts.add(new Context(clazz.getMethod("blue")));
        contexts.add(new Context(clazz.getMethod("green")));
        contexts.add(new Context(clazz.getMethod("green")));
        contexts.add(new Context(clazz.getMethod("orange")));

        Random random = new Random();
        StatsInterceptor interceptor = new StatsInterceptor(clazz);

        interceptor.invoke(contexts.get(random(random, 0, contexts.size())));
        interceptor.invoke(contexts.get(random(random, 0, contexts.size())));
        
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        server.registerMBean(new ManagedMBean(interceptor), new ObjectName("something:name=Invocations"));

        snooze(5000);
        while (true) {
            interceptor.invoke(contexts.get(random(random, 0, contexts.size())));
        }
//        new CountDownLatch(1).await();

    }

    @Monitor
    public static class MyBean {
        public void red(){}
        public void green(){}
        public void blue(){}
        public void orange(){}
    }

    private void snooze(int millis) {
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

    private class Context implements InvocationContext {

        private final Random random = new Random();

        private final Method method;

        private Context(Method method) {
            this.method = method;
        }

        public Object getTarget() {
            return null;
        }

        public Method getMethod() {
            return method;
        }

        public Object[] getParameters() {
            return new Object[0];
        }

        public void setParameters(Object[] objects) {
        }

        public Map<String, Object> getContextData() {
            return null;
        }

        public Object proceed() throws Exception {
            snooze((int) (System.nanoTime() / 1000 % 1000));

            return null;
        }
    }

}
