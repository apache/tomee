package org.apache.openejb.server.groovy;

import javax.inject.Named;

@Named
public class Bar {
    public String test() {
        return "ok";
    }
}
