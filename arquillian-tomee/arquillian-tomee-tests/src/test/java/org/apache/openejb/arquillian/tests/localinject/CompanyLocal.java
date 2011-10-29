package org.apache.openejb.arquillian.tests.localinject;

import javax.ejb.Local;

@Local
public interface CompanyLocal extends Company {
    public String employ(String employeeName);
}