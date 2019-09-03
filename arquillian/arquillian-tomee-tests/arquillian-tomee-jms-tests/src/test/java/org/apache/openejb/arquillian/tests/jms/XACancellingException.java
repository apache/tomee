package org.apache.openejb.arquillian.tests.jms;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class XACancellingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public XACancellingException() {
      super("Should rollback");
      }
}
