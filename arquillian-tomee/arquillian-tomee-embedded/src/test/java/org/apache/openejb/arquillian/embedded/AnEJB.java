package org.apache.openejb.arquillian.embedded;

import javax.ejb.Singleton;

@Singleton
public class AnEJB {
    public String test() {
        return "ok";
    }
}
