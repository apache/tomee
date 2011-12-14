package org.apache.tomee.catalina.facade;

import javax.ejb.Remote;

/**
 * @author rmannibucau
 */
@Remote
public interface ExceptionManagerFacade {
    Exception exception();
}
