package org.apache.openejb.spi;

import java.util.Properties;

import javax.transaction.TransactionManager;

import org.apache.openejb.OpenEJBException;

public interface Assembler {

    public void init(Properties props) throws OpenEJBException;

    public void build() throws OpenEJBException;

    public ContainerSystem getContainerSystem();

    public TransactionManager getTransactionManager();

    public SecurityService getSecurityService();

}