package org.apache.tomee.loader.test;

import javax.ejb.Stateless;

@Stateless
public class DummyEjb {

    public String sayHi() {
        return "Hi, buddy!";
    }
}
