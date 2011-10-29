package org.apache.openejb.arquillian.tests.remote;

import javax.ejb.Stateless;

@Stateless
public class DefaultCompany implements CompanyRemote {

    private final String name = "TomEE Software Inc.";

    public String employ(String employeeName) {
        return employeeName + " is employed at " + name;
    }

}