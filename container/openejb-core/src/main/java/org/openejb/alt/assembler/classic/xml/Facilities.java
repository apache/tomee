package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.FacilitiesInfo;
import org.w3c.dom.Node;

public class Facilities extends FacilitiesInfo implements DomObject{

    public static final String INTRA_VM_SERVER = "intra-vm-server";

    public static final String REMOTE_JNDI_CONTEXTS = "remote-jndi-contexts";

    public static final String JNDI_CONTEXT = "jndi-context";

    public static final String CONNECTORS = "connectors";

    public static final String CONNECTOR = "connector";
    public static final String CONNECTION_MANAGER = "connection-manager";
    public static final String NODES = "nodes";

    public static final String SERVICES = "services";

    public static final String SECURITY_SERVICE = "security-service";

    public static final String TRANSACTION_SERVICE = "transaction-service";

    public void initializeFromDOM(Node node) throws OpenEJBException{

        /* IntraVmServer
        intraVmServer = (IntraVmServer) DomTools.collectChildElementByType(node, IntraVmServer.class, INTRA_VM_SERVER);
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        /* Jndi Contexts
        Node jndiContextsElement = DomTools.getChildElement(node, REMOTE_JNDI_CONTEXTS);
        if(jndiContextsElement !=null){
        DomObject[] dos = DomTools.collectChildElementsByType(jndiContextsElement, JndiContext.class, JNDI_CONTEXT);
        remoteJndiContexts = new JndiContext[dos.length];
        for (int i=0; i < dos.length; i++) remoteJndiContexts[i] = (JndiContext)dos[i];
        }
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        /* Connector
        Node connectorsElement = DomTools.getChildElement(node, CONNECTORS);
        if(connectorsElement != null){
        DomObject[] dos = DomTools.collectChildElementsByType(connectorsElement, Connector.class, CONNECTOR);
        connectors = new Connector[dos.length];
        for (int i=0; i < dos.length; i++) connectors[i] = (Connector)dos[i];
        }
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        /* ConnectionManager
        if(connectorsElement != null){
        DomObject[] dos = DomTools.collectChildElementsByType(connectorsElement, ConnectionManager.class, CONNECTION_MANAGER);
        connectionManagers = new ConnectionManager[dos.length];
        for (int i=0; i < dos.length; i++) connectionManagers[i] = (ConnectionManager)dos[i];
        }
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        /* SecurityService
        Node servicesElement = DomTools.getChildElement(node, SERVICES);
        securityService = (SecurityService)DomTools.collectChildElementByType(servicesElement, SecurityService.class, SECURITY_SERVICE);
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        /* TransactionService
        transactionService = (TransactionService)DomTools.collectChildElementByType(servicesElement, TransactionService.class, TRANSACTION_SERVICE);
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
