package org.apache.openejb.arquillian.tests.remote;

import javax.ejb.Remote;

@Remote
public interface CompanyRemote {
    public String employ(String employeeName);
}