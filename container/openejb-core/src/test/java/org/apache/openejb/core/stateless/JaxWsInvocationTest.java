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
package org.apache.openejb.core.stateless;

import junit.framework.TestCase;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.AfterClass;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The point of this test case is to verify that OpenEJB is accurately performing
 * it's part of a WebServiceProvider to OpenEJB invocation as it relates to JAX-RPC.
 *
 * In the agreement between OpenEJB and the Web Service Provider, the Web Service Provider
 * must supply the MessageContext and an Interceptor as the arguments of the standard
 * container.invoke method call.
 *
 * OpenEJB must ensure the MessageContext is exposed via the SessionContext.getMessageContext
 * and ensure that the interceptor is added to the chain just after the other interceptors and
 * before the bean method itself is invoked.
 *
 * @version $Rev$ $Date$
 */
public class JaxWsInvocationTest extends TestCase {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    public void testWsInvocations() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class, "PseudoSecurityService", null, "PseudoSecurityService", null));

        assembler.createContainer(config.configureService(StatelessSessionContainerInfo.class));


        final EjbJarInfo ejbJar = config.configureApplication(buildTestApp());

        assembler.createApplication(ejbJar);

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        final BeanContext beanContext = containerSystem.getBeanContext("EchoBean");

        assertNotNull(beanContext);

        assertEquals("ServiceEndpointInterface", EchoServiceEndpoint.class, beanContext.getServiceEndpointInterface());


        // OK, Now let's fake a web serivce invocation coming from any random
        // web service provider.  The web serivce provider needs supply
        // the MessageContext and an interceptor to do the marshalling as
        // the arguments of the standard container.invoke signature.

        // So let's create a fake message context.
        final MessageContext messageContext = new FakeMessageContext();

        // Now let's create a fake interceptor as would be supplied by the
        // web service provider.  Instead of writing "fake" marshalling
        // code that would pull the arguments from the soap message, we'll
        // just give it the argument values directly.
        final Object wsProviderInterceptor = new FakeWsProviderInterceptor("Hello world");

        // Ok, now we have the two arguments expected on a JAX-RPC Web Service
        // invocation as per the OpenEJB-specific agreement between OpenEJB
        // and the Web Service Provider
        final Object[] args = new Object[]{messageContext, wsProviderInterceptor};

        // Let's grab the container as the Web Service Provider would do and
        // perform an invocation
        final RpcContainer container = (RpcContainer) beanContext.getContainer();

        final Method echoMethod = EchoServiceEndpoint.class.getMethod("echo", String.class);

        final String value = (String) container.invoke("EchoBean", InterfaceType.SERVICE_ENDPOINT, echoMethod.getDeclaringClass(), echoMethod, args, null);

        assertCalls(Call.values());
        calls.clear();
        assertEquals("Hello world", value);

    }

    private void assertCalls(final Call... expectedCalls) {
        final List expected = Arrays.asList(expectedCalls);
        assertEquals(join("\n", expected), join("\n", calls));
    }

    public static enum Call {
        WebServiceProvider_Invoke_BEFORE,
        EjbInterceptor_Invoke_BEFORE,
        Bean_Invoke_BEFORE,
        Bean_Invoke,
        Bean_Invoke_AFTER,
        EjbInterceptor_Invoke_AFTER,
        WebServiceProvider_Invoke_AFTER,
    }

    public static List<Call> calls = new ArrayList<Call>();

    public EjbModule buildTestApp() {
        final EjbJar ejbJar = new EjbJar();

        final StatelessBean bean = ejbJar.addEnterpriseBean(new StatelessBean(EchoBean.class));
        bean.setServiceEndpoint(EchoServiceEndpoint.class.getName());

        return new EjbModule(this.getClass().getClassLoader(), this.getClass().getSimpleName(), "test", ejbJar, null);
    }

    @Interceptors({PlainEjbInterceptor.class})
    @WebService
    public static class EchoBean {

        public EchoBean() {
        }

        @Resource
        private SessionContext ctx;

        @Resource
        private WebServiceContext wsContext;

        @AroundInvoke
        public Object invoke(final InvocationContext context) throws Exception {

            /**
             * For JAX-WS invocations context.getContextData() must return the 
             * JAX-WS MessageContex. As per the agreement between OpenEJB and the Web Service Provider
             * the MessageContex should have been passed into the container.invoke method
             * and the container should then ensure it's available via getContextData()
             * for the duration of this call.
             */
            final MessageContext messageContext = (MessageContext) context.getContextData();

            org.junit.Assert.assertNotNull("message context should not be null", messageContext);
            org.junit.Assert.assertTrue("the Web Service Provider's message context should be used", messageContext instanceof FakeMessageContext);

            // Try to get JAX-RPC context, should throw an exception since it's JAX-WS
            try {
                ctx.getMessageContext();
                org.junit.Assert.fail("Did not throw exception");
            } catch (final IllegalStateException e) {
                // that's expected since it's JAX-WS
            }

            // test @Resource WebServiceContext injection
            org.junit.Assert.assertNotNull("web service context should not be null", wsContext);
            org.junit.Assert.assertEquals("msg context should be the smae", messageContext, wsContext.getMessageContext());

            org.junit.Assert.assertFalse("user in role 'foo'", wsContext.isUserInRole("foo"));
            org.junit.Assert.assertNotNull("user principal", wsContext.getUserPrincipal());

            calls.add(Call.Bean_Invoke_BEFORE);
            final Object o = context.proceed();
            calls.add(Call.Bean_Invoke_AFTER);
            return o;
        }

        public String echo(final String data) {
            calls.add(Call.Bean_Invoke);
            return data;
        }
    }

    @WebService
    public static interface EchoServiceEndpoint {
        String echo(String data);
    }

    /**
     * This interceptor is here to ensure that the container
     * still invokes interceptors normally for web serivce
     * invocations and to also guarantee that the Web Service
     * Provider's interceptor (which is a special OpenEJB concept)
     * is invoked *after* all the normal ejb interceptors.
     */
    public static class PlainEjbInterceptor {

        public PlainEjbInterceptor() {
        }

        @AroundInvoke
        public Object invoke(final InvocationContext context) throws Exception {
            // Track this call so we can assert proper interceptor order
            calls.add(Call.EjbInterceptor_Invoke_BEFORE);
            final Object o = context.proceed();
            calls.add(Call.EjbInterceptor_Invoke_AFTER);
            return o;
        }
    }


    private static String join(final String delimeter, final List items) {
        final StringBuilder sb = new StringBuilder();
        for (final Object item : items) {
            sb.append(item.toString()).append(delimeter);
        }
        return sb.toString();
    }


    /**
     * This object would be implemented by the Web Service Provider per
     * the JAX-WS spec and supplied to us in the container.invoke method
     * per the OpenEJB-WebServiceProvider agreement
     */
    public static class FakeMessageContext implements MessageContext {

        private final Map map = new HashMap();

        public void clear() {
            map.clear();
        }

        public boolean containsKey(final Object key) {
            return map.containsKey(key);
        }

        public boolean containsValue(final Object value) {
            return map.containsValue(value);
        }

        public Set<Entry<String, Object>> entrySet() {
            return map.entrySet();
        }

        public Object get(final Object key) {
            return map.get(key);
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }

        public Set<String> keySet() {
            return map.keySet();
        }

        public Object put(final String key, final Object value) {
            return map.put(key, value);
        }

        public void putAll(final Map<? extends String, ? extends Object> t) {
            map.putAll(t);
        }

        public Object remove(final Object key) {
            return map.remove(key);
        }

        public int size() {
            return map.size();
        }

        public Collection<Object> values() {
            return map.values();
        }

        public Scope getScope(final String arg0) {
            return null;
        }

        public void setScope(final String arg0, final Scope arg1) {
        }

    }

    /**
     * This object would be supplied by the Web Service Provider
     * as per the OpenEJB-WebServiceProvider agreement and serves
     * two purposes:
     *
     * 1. Executing the Handler Chain (as required by
     * the JAX-RPC specification) in the context of the EJB Container
     * (as required by the EJB and J2EE WebServices specifications)
     *
     * 2. Unmarshalling the method arguments from the SOAP message
     * after the handlers in the Handler Chain have had a chance
     * to modify the argument values via the SAAJ tree.
     *
     * The Interceptor instance given to OpenEJB is constructed
     * and created by the Web Service Provider and should contain
     * all the data it requires to complete it's part of the agreement.
     *
     * OpenEJB will not perform any injection on this object and
     * the interceptor will be discarded so that the Web Service
     * Provider may pass in a new Interceptor instance on every
     * web service invocation.
     *
     * The Web Service Provider may pass in any object to serve
     * the roll of the Interceptor as long as it has an @AroundInvoke
     * method using the method signature:
     *
     * public Object <METHOD-NAME> (InvocationContext ctx) throws Exception
     *
     * Unlike typical EJB Interceptor around invoke methods, the @AroundInvoke
     * annotation must be used and is not optional, and the method must be public.
     */
    public static class FakeWsProviderInterceptor {

        public FakeWsProviderInterceptor() {
            this(new Object[]{});
        }

        /**
         * These would normally come from the soap message
         */
        private final Object[] args;

        public FakeWsProviderInterceptor(final Object... args) {
            this.args = args;
        }

        @AroundInvoke
        public Object invoke(final InvocationContext invocationContext) throws Exception {
            // The interceptor of the web serivce must set the
            // arguments it marshalls from the soap message into
            // the InvocationContext so we can invoke the bean.
            invocationContext.setParameters(args);

            final Object returnValue;
            try {

                // Track this call so we can assert proper interceptor order
                calls.add(Call.WebServiceProvider_Invoke_BEFORE);

                // handler chain "before advice" would happen here
                returnValue = invocationContext.proceed();
                // handler chain "after advice" would happen here

                // Track this call so we can assert proper interceptor order
                calls.add(Call.WebServiceProvider_Invoke_AFTER);

            } catch (final Exception e) {
                // handler chain fault processing would happen here
                throw e;
            }
            return returnValue;
        }
    }
}
