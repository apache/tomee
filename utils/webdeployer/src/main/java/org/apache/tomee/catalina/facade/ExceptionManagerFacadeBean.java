package org.apache.tomee.catalina.facade;

import org.apache.openejb.assembler.classic.DeploymentExceptionManager;
import org.apache.openejb.loader.SystemInstance;

import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

@Singleton(name = "openejb/ExceptionManagerFacade")
@TransactionManagement(TransactionManagementType.BEAN)
public class ExceptionManagerFacadeBean implements ExceptionManagerFacade {
    public Exception exception() {
        return SystemInstance.get().getComponent(DeploymentExceptionManager.class).getFirstException();
    }
}
