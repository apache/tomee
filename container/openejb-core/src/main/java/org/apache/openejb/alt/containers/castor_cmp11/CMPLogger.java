package org.apache.openejb.alt.containers.castor_cmp11;

import org.apache.openejb.util.Logger;

public class CMPLogger implements org.exolab.castor.persist.spi.LogInterceptor {
    protected final Logger logger = Logger.getInstance("OpenEJB.CastorCMP", "org.apache.openejb.alt.util.resources");
    protected final String db;

    public CMPLogger(String db) {
        this.db = db + ": ";
    }

    public void loading(java.lang.Object objClass, java.lang.Object identity) {
        logger.debug(db + "Loading an instance of " + objClass + " with identity \"" + identity + "\"");
    }

    public void creating(java.lang.Object objClass, java.lang.Object identity) {
        logger.debug(db + "Creating an instance of " + objClass + " with identity \"" + identity + "\"");
    }

    public void removing(java.lang.Object objClass, java.lang.Object identity) {
        logger.debug(db + "Removing an instance of " + objClass + " with identity \"" + identity + "\"");
    }

    public void storing(java.lang.Object objClass, java.lang.Object identity) {
        logger.debug(db + "Storing an instance of " + objClass + " with identity \"" + identity + "\"");
    }

    public void storeStatement(java.lang.String statement) {
        logger.debug(db + statement);
    }

    public void queryStatement(java.lang.String statement) {
        logger.debug(db + statement);
    }

    public void message(java.lang.String message) {
        logger.info(db + "JDO message:" + message);
    }

    public void exception(java.lang.Exception ex) {
        logger.info(db + "JDO exception:", ex);
    }

    public java.io.PrintWriter getPrintWriter() {
        return null;
    }
}

