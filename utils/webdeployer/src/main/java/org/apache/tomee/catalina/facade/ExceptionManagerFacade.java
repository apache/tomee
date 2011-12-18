package org.apache.tomee.catalina.facade;

import javax.ejb.Remote;

@Remote
public interface ExceptionManagerFacade {
    Exception exception();
}
