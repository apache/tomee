package org.apache.openejb.arquillian.tests.filterremote;

import javax.ejb.Remote;

@Remote
public interface CompanyRemote {
    public String employ(String employeeName);
}