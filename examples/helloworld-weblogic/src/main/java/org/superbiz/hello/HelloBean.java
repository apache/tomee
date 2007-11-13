/* =====================================================================
 *
 * Copyright (c) 2003 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.superbiz.hello;

import javax.ejb.LocalHome;
import javax.ejb.Stateless;

/**
 * @version $Revision$ $Date$
 */
@Stateless
@LocalHome(HelloEjbLocalHome.class)
public class HelloBean {

    public String sayHello() {
        return "Hello, World!";
    }

}
