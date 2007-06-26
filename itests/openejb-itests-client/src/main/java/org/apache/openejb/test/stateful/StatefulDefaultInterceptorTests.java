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
// public class StatelessInterceptorTests extends AnnotatedFieldInjectionStatelessLocalTestClient {
public class StatefulDefaultInterceptorTests extends BasicStatefulLocalTestClient {

    /*
     * @EJB(name="StatefulInterceptedBusinessRemote", beanInterface = BasicStatelessInterceptedRemote.class)
     */
    private BasicStatefulInterceptedRemote firstBean;
    private BasicStatefulInterceptedRemote secondBean;
    private BasicStatefulInterceptedRemote thirdBean;

    public StatefulDefaultInterceptorTests() {
        super("BasicStatefulIntercepted.");
    }

    public void testNothing() {
    }

//
//  TODO lookups are broken
//
//    protected void setUp() throws Exception {
//        super.setUp();
//        Object obj = initialContext.lookup("StatefulInterceptedBusinessRemote");
//        assertNotNull("The StatefulInterceptedBusinessRemote object is null", obj);
//        firstBean = (BasicStatefulInterceptedRemote) javax.rmi.PortableRemoteObject.narrow(obj,
//                BasicStatefulInterceptedRemote.class);
//        assertNotNull("Default interceptor is null", firstBean);
//
//        obj = initialContext.lookup("SecondStatefulInterceptedBusinessRemote");
//        assertNotNull("The StatefulInterceptedBusinessRemote object is null", obj);
//        secondBean = (BasicStatefulInterceptedRemote) javax.rmi.PortableRemoteObject.narrow(obj,
//                BasicStatefulInterceptedRemote.class);
//        assertNotNull("Default interceptor is null", secondBean);
//
//        obj = initialContext.lookup("ThirdStatefulInterceptedBusinessRemote");
//        assertNotNull("The StatefulInterceptedBusinessRemote object is null", obj);
//        thirdBean = (BasicStatefulInterceptedRemote) javax.rmi.PortableRemoteObject.narrow(obj,
//                BasicStatefulInterceptedRemote.class);
//        assertNotNull("ThirdStatefulInterceptedBean is null", thirdBean);
//    }
//
//    /**
//     * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
//     */
//    protected void tearDown() throws Exception {
//        super.tearDown();
//    }
//
//
//
//    /**
//     * Invokes a business method which is annotated to be excluded from interception.
//     * <code>getContextData()</code> has been annotated with <code>@ExcludesDefaultInterceptors</code>
//     */
//    public void test01_excludeDefaultInterceptorsOnMethod() {
//        Map contextData = firstBean.getContextData();
//        // verifying that inBeanInterceptor indeed intercepted this method. This cannot be excluded at all.
//        assertNotNull(contextData.containsKey("getContextData"));
//
//        Map innerMap = (Map) contextData.get("getContextData");
//        ArrayList interceptorsList = (ArrayList) innerMap.get("INTERCEPTORS");
//        // verifying @ExcludeClassInterceptors annotated method was not intercepted
//        assertFalse("getContextData() should not have been intercepted by classInterceptor()", interceptorsList.contains("defaultInterceptor"));
//    }
//
//    /**
//     * Invokes a business method which is annotated to be excluded from interception.
//     * <code>getContextData()</code> has been defined with <code>excludes-default-interceptors</code>
//     */
//    public void test02_excludeDefaultInterceptorsOnMethod() {
//        Map contextData = secondBean.getContextData();
//        // verifying that inBeanInterceptor indeed intercepted this method. This cannot be excluded at all.
//        assertNotNull(contextData.containsKey("getContextData"));
//
//        Map innerMap = (Map) contextData.get("getContextData");
//        ArrayList interceptorsList = (ArrayList) innerMap.get("INTERCEPTORS");
//        // verifying @ExcludeClassInterceptors annotated method was not intercepted
//        assertFalse("getContextData() should not have been intercepted by classInterceptor()", interceptorsList.contains("defaultInterceptor"));
//    }
//
//    /**
//     * Invokes a business method which is annotated to be excluded from interception.
//     * <code>getContextData()</code> has been defined with <code>excludes-default-interceptors</code>
//     */
//    public void test03_excludeDefaultInterceptorsOnMethod() {
//        Map contextData = thirdBean.getContextData();
//        // verifying that inBeanInterceptor indeed intercepted this method. This cannot be excluded at all.
//        assertNotNull(contextData.containsKey("getContextData"));
//
//        Map innerMap = (Map) contextData.get("getContextData");
//        ArrayList interceptorsList = (ArrayList) innerMap.get("INTERCEPTORS");
//        // verifying @ExcludeClassInterceptors annotated method was not intercepted
//        assertFalse("getContextData() should not have been intercepted by classInterceptor()", interceptorsList.contains("defaultInterceptor"));
//    }
//
}
