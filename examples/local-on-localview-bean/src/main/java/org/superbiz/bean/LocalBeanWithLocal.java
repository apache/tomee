package org.superbiz.bean;

import javax.ejb.Local;
import javax.ejb.Stateless;

@Local
@Stateless(mappedName = "Foo")
public class LocalBeanWithLocal {
    public String msg() {
        return "@Local shouldn't be put on a bean without interface. It works with glassfish so tolerating it.";
    }
}
