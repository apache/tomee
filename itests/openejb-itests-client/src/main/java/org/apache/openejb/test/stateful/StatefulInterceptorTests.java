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
package org.apache.openejb.test.stateful;

import java.util.ArrayList;
import java.util.Map;

// import javax.ejb.EJB;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:goyathlay.geronimo@gmail.com">Prasad Kashyap</a>
 */
// public class StatefulInterceptorTests extends AnnotatedFieldInjectionStatefulLocalTestClient {
public class StatefulInterceptorTests extends BasicStatefulLocalTestClient {

    /*
     * @EJB(name="BasicStatefulInterceptedBusinessRemote", beanInterface = BasicStatefulInterceptedRemote.class)
     */
    private BasicStatefulInterceptedRemote remoteInterceptor;

    public StatefulInterceptorTests() {
        super("BasicStatefulIntercepted.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("BasicStatefulInterceptedBusinessRemote");
        assertNotNull("The object is null", obj);
        remoteInterceptor = (BasicStatefulInterceptedRemote) javax.rmi.PortableRemoteObject.narrow(obj,
                BasicStatefulInterceptedRemote.class);
        assertNotNull("Remote interceptor is null", remoteInterceptor);
    }

    /**
     * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    
    /**
     * Invokes a business method which is to be intercepted by class, in-bean and at method level.
     */
    public void test01_interceptorChaining() {
        String reverseMe = "Intercept";
        String reversedString = remoteInterceptor.reverse(reverseMe);
        // verifying InvocationContext.procced()
        assertEquals("tpecretnI", reversedString);
        
        Map contextData = remoteInterceptor.getContextData();
        // verifying that inBeanInterceptor indeed intercepted this method. This cannot be excluded at all.
        assertNotNull(contextData.containsKey("reverse"));
        
        Map innerMap = (Map) contextData.get("reverse");
        ArrayList interceptorsList = (ArrayList) innerMap.get("INTERCEPTORS");
        // verifying interceptor chaining order
        assertEquals("superClassInterceptor", interceptorsList.get(0)); //derived from class extension
        assertEquals("classInterceptor", interceptorsList.get(1)); //specified by @
        assertEquals("secondClassInterceptor", interceptorsList.get(2)); //specified by @
        assertEquals("methodInterceptor", interceptorsList.get(3)); //specified by @ on method
        assertEquals("ddInterceptor", interceptorsList.get(4)); //specified in DD on method
        assertEquals("superBeanInterceptor", interceptorsList.get(5));  //derived from bean extension
        assertEquals("inBeanInterceptor", interceptorsList.get(6)); //in bean
    }
    
    /**
     * Invokes just 1 business method on the bean. The interceptor method stores the intercepted method's name and
     * params in a map that is returned by the <code>getContextData</code>
     */
    public void test02_methodProfile() {
        String reverseMe = "Intercept";
        String reversedString = remoteInterceptor.reverse(reverseMe);
        // verifying InvocationContext.procced()
        assertEquals("tpecretnI", reversedString);

        Map contextData = remoteInterceptor.getContextData();
        // verifying InvocationContext.getMethod().getName()
        assertTrue(contextData.containsKey("reverse"));

        Map innerMap = (Map) contextData.get("reverse");
        Object[] params = (Object[]) innerMap.get("PARAMETERS");
        // verifying that the parameters array was received from contextData and stored.
        assertNotNull("value of PARAMETERS key is null", params);
        // verifying InvocationContext.getParameters()
        assertEquals(1, params.length);
        assertEquals(reverseMe, params[0].toString());
    }
    

    /**
     * Invokes a business method which is annotated to be excluded from interception.
     * <code>getContextData()</code> has been annotated with <code>@ExcludesClassInterceptors</code>
     */
    public void test03_excludeClassInterceptors() {
        Map contextData = remoteInterceptor.getContextData();
        // verifying that inBeanInterceptor indeed intercepted this method. This cannot be excluded at all.
        assertNotNull(contextData.containsKey("getContextData"));
        
        Map innerMap = (Map) contextData.get("getContextData");
        ArrayList interceptorsList = (ArrayList) innerMap.get("INTERCEPTORS");
        // verifying @ExcludeClassInterceptors annotated method was not intercepted by class interceptors
        assertFalse("getContextData() should not have been intercepted by superClassInterceptor()", interceptorsList.contains("superClassInterceptor"));
        assertFalse("getContextData() should not have been intercepted by classInterceptor()", interceptorsList.contains("classInterceptor"));
        assertFalse("getContextData() should not have been intercepted by secondClassInterceptor()", interceptorsList.contains("secondClassInterceptor"));
        assertFalse("getContextData() should not have been intercepted by ddInterceptor()", interceptorsList.contains("ddInterceptor"));
    }
    
    /**
     * Invokes a business method which is declared to be excluded from interception by the DD
     * <code>getContextData()</code> has been annotated with <code>@ExcludesClassInterceptors</code>
     */
    public void test04_excludeClassInterceptors_02() {
        String catString = remoteInterceptor.concat("Inter", "cept");
        // verifying InvocationContext.procced()
        assertEquals("Intercept", catString);
        
        Map contextData = remoteInterceptor.getContextData();
        // verifying that inBeanInterceptor indeed intercepted this method. This cannot be excluded at all.
        assertNotNull(contextData.containsKey("concat"));
        
        Map innerMap = (Map) contextData.get("concat");
        ArrayList interceptorsList = (ArrayList) innerMap.get("INTERCEPTORS");
        // verifying @ExcludeClassInterceptors annotated method was not intercepted
        assertFalse("concat() should not have been intercepted by superClassInterceptor()", interceptorsList.contains("superClassInterceptor"));
        assertFalse("concat() should not have been intercepted by classInterceptor()", interceptorsList.contains("classInterceptor"));
        assertFalse("concat() should not have been intercepted by ddInterceptor()", interceptorsList.contains("ddInterceptor"));
        assertFalse("concat() should not have been intercepted by secondClassInterceptor()", interceptorsList.contains("secondClassInterceptor"));
    }
    
    /**
     * Invokes a business method which is annotated to be excluded from interception.
     * <code>getContextData()</code> has been annotated with <code>@ExcludesClassInterceptors</code>
     */
    public void test05_PreDestroy() {
        try {
            tearDown();
            Map contextData = remoteInterceptor.getContextData();
            Map innerMap = (Map) contextData.get("BasicStatefulInterceptedBean");
            assertNotNull("InnerMap is null", innerMap);
            ArrayList interceptorsList = (ArrayList) innerMap.get("INTERCEPTORS");
            // verifying interceptor chaining order
            assertEquals("superClassInterceptorPreDestroy", interceptorsList.get(0));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
