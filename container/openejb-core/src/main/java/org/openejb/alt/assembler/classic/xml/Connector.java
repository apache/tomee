package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.ConnectorInfo;
import org.w3c.dom.Node;

public class Connector extends ConnectorInfo implements DomObject{

    public static final String CONNECTOR_ID = "connector-id";
    public static final String CONNECTION_MANAGER_ID = "connection-manager-id";

    public static final String MANAGED_CONNECTION_FACTORY = "managed-connection-factory";

    public void initializeFromDOM(Node node) throws OpenEJBException{

        connectorId = DomTools.getChildElementPCData(node, CONNECTOR_ID);

        connectionManagerId = DomTools.getChildElementPCData(node, CONNECTION_MANAGER_ID);

        managedConnectionFactory = (ManagedConnectionFactory)DomTools.collectChildElementByType(node, ManagedConnectionFactory.class, MANAGED_CONNECTION_FACTORY);

    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}

