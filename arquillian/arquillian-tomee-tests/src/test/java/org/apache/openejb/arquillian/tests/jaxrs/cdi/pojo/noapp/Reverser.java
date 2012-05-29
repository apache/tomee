package org.apache.openejb.arquillian.tests.jaxrs.cdi.pojo.noapp;

public class Reverser {
    public String reverse(final String msg) {
        return new StringBuilder(msg).reverse().toString();
    }
}
