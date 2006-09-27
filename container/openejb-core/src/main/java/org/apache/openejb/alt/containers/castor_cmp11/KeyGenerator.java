package org.apache.openejb.alt.containers.castor_cmp11;

import org.exolab.castor.persist.spi.Complex;

public interface KeyGenerator {

    public Object getPrimaryKey(javax.ejb.EntityBean bean);

    public Complex getJdoComplex(Object primaryKey);

    public boolean isKeyComplex();

}