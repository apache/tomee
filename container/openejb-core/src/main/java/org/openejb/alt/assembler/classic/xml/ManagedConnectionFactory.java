package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.ManagedConnectionFactoryInfo;
import org.w3c.dom.Node;

public class ManagedConnectionFactory extends ManagedConnectionFactoryInfo implements DomObject {

    public static final String CLASS_NAME = "class-name";

    public static final String CODEBASE = "codebase";

    public static final String ID = "connection-factory-id";

    public void initializeFromDOM(Node node) throws OpenEJBException{

        className = DomTools.getChildElementPCData(node, CLASS_NAME);
        codebase = DomTools.getChildElementPCData(node, CODEBASE);
        id = DomTools.getChildElementPCData(node, ID);

        properties = DomTools.readProperties(node);

    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
