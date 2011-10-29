package org.apache.openejb.arquillian.tests.listenerremote;

import javax.ejb.Remote;

@Remote
public interface CompanyRemote {
    public String employ(String employeeName);
}