package org.apache.openejb.arquillian.tests.localinject;

import javax.ejb.Stateless;

@Stateless
public class DefaultCompany implements CompanyLocal {

    private final String name = "TomEE Software Inc.";

    public String employ(String employeeName) {
        return employeeName + " is employed at " + name;
    }

}