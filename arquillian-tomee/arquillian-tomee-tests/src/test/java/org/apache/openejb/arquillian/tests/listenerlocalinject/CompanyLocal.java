package org.apache.openejb.arquillian.tests.listenerlocalinject;

import javax.ejb.Local;

@Local
public interface CompanyLocal extends Company {
}