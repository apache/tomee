package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.ConnectionManagerInfo;
import org.w3c.dom.Node;

public class ConnectionManager extends ConnectionManagerInfo  implements DomObject{

    public static final String CLASS_NAME = "class-name";
    public static final String CONNECTION_MANAGER_ID = "connection-manager-id";

    public static final String CODEBASE = "codebase";

    public void initializeFromDOM(Node node) throws OpenEJBException{

        className = DomTools.getChildElementPCData(node, CLASS_NAME);
        connectionManagerId = DomTools.getChildElementPCData(node, CONNECTION_MANAGER_ID);
        codebase = DomTools.getChildElementPCData(node, CODEBASE);

        properties = DomTools.readProperties(node);

    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
