package org.apache.openejb.assembler.classic;

public class FacilitiesInfo extends InfoObject {

    public IntraVmServerInfo intraVmServer;
    public JndiContextInfo [] remoteJndiContexts;
    public ConnectorInfo [] connectors;
    public ConnectionManagerInfo [] connectionManagers;
    public TransactionServiceInfo transactionService;
    public SecurityServiceInfo securityService;

}
