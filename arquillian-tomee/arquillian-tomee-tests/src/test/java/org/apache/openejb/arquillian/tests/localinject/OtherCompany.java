package org.apache.openejb.arquillian.tests.localinject;

import javax.ejb.Stateless;

@Stateless
public class OtherCompany implements CompanyLocal {

    private final String name = "Other Software Inc.";

    public String employ(String employeeName) {
        return employeeName + " is employed at " + name;
    }

}