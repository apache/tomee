package org.apache.openejb.assembler.classic;

import java.util.Properties;

import org.apache.openejb.OpenEJBException;

public interface OpenEjbConfigurationFactory {

    public void init(Properties props) throws OpenEJBException;

    public OpenEjbConfiguration getOpenEjbConfiguration() throws OpenEJBException;

    /*
     * Not used yet.
     * Will be used in the future to give the ability to load and reload container systems
     * after OpenEJB has been started.
    public ContainerSystemInfo getContainerSystemInformation()throws OpenEJBException;

    */

}
