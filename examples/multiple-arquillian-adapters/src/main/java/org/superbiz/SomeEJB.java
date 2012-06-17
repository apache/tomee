package org.superbiz;

import javax.ejb.Stateless;
import javax.jws.WebService;

@Stateless
public class SomeEJB {
    public String ok() {
        return "ejb";
    }
}
