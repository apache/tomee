package org.apache.openejb.arquillian.embedded;

import javax.ejb.Singleton;

/**
 * @author rmannibucau
 */
@Singleton
public class AnEJB {
    public String test() {
        return "ok";
    }
}
