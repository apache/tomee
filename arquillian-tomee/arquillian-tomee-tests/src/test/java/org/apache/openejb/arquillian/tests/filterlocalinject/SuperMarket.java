package org.apache.openejb.arquillian.tests.filterlocalinject;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class SuperMarket {

    private final String name = "Apache Marketplace";

    public String shop(String employeeName) {
        return employeeName + " shops at " + name;
    }

}