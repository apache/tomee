/* =====================================================================
 *
 * Copyright (c) 2003 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.superbiz.hello;

import junit.framework.TestCase;

import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class HelloTest extends TestCase {

    public void test() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        InitialContext initialContext = new InitialContext(properties);

        HelloEjbLocalHome localHome = (HelloEjbLocalHome) initialContext.lookup("MyHello");
        HelloEjbLocal helloEjb = localHome.create();

        String message = helloEjb.sayHello();

        assertEquals(message, "Hello, World!");
    }
}
