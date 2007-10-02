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
package org.apache.openejb.test.stateless;

import java.util.ArrayList;
import java.util.Map;
import java.util.Arrays;

// import javax.ejb.EJB;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:goyathlay.geronimo@gmail.com">Prasad Kashyap</a>
 */
// public class StatelessInterceptorTests extends AnnotatedFieldInjectionStatelessLocalTestClient {
public class StatelessInterceptorTests extends BasicStatelessLocalTestClient {

    /*
     * @EJB(name="BasicStatelessInterceptedBusinessRemote", beanInterface = BasicStatelessInterceptedRemote.class)
     */
    private BasicStatelessInterceptedRemote remoteInterceptor;

    public StatelessInterceptorTests() {
        super("BasicStatelessIntercepted.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("BasicStatelessInterceptedBusinessRemote");
        assertNotNull("The BasicStatelessInterceptedBusinessRemote object is null", obj);
        remoteInterceptor = (BasicStatelessInterceptedRemote) javax.rmi.PortableRemoteObject.narrow(obj,
                BasicStatelessInterceptedRemote.class);
        assertNotNull("Remote interceptor is null", remoteInterceptor);
    }

    /**
     * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    
    /**
     * Invokes a business method which is to be intercepted from a class, in-bean and at method level.
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
        assertEquals(Arrays.asList("ddInterceptor","secondClassInterceptor", "superClassInterceptor", "classInterceptor", "methodInterceptor", "superBeanInterceptor","inBeanInterceptor"), interceptorsList);

        assertEquals("ddInterceptor", interceptorsList.get(0)); //specified in DD
        assertEquals("secondClassInterceptor", interceptorsList.get(1)); //specified in DD
        assertEquals("superClassInterceptor", interceptorsList.get(2)); //derived from class extension
        assertEquals("classInterceptor", interceptorsList.get(3)); //specified by @        
        assertEquals("methodInterceptor", interceptorsList.get(4)); //specified by @ on method
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
        // verifying @ExcludeClassInterceptors annotated method was not intercepted
        assertFalse("getContextData() should not have been intercepted by classInterceptor()", interceptorsList.contains("classInterceptor"));
        assertFalse("getContextData() should not have been intercepted by classInterceptor()", interceptorsList.contains("classInterceptor"));
        assertFalse("getContextData() should not have been intercepted by secondClassInterceptor()", interceptorsList.contains("secondClassInterceptor"));
        assertFalse("getContextData() should not have been intercepted by ddInterceptor()", interceptorsList.contains("ddInterceptor"));
    }
    
    /**
     * Invokes a business method which is declared to be excluded from interception by the DD
     * <code>concat()</code> has been annotated with <code>exclude-class-interceptors</code>
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
    
    

}
