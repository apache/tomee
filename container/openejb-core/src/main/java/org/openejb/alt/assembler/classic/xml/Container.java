package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.ContainerInfo;
import org.w3c.dom.Node;

public class Container extends ContainerInfo {

    public static final String DESCRIPTION = "description";

    public static final String DISPLAY_NAME = "display-name";

    public static final String CONTAINER_NAME = "container-name";

    public static final String CODEBASE = "codebase";

    public static final String CLASSNAME = "class-name";

    public static void initializeFromDOM(Node node, ContainerInfo container) throws OpenEJBException{
        container.description = DomTools.getChildElementPCData(node, DESCRIPTION);
        container.displayName = DomTools.getChildElementPCData(node, DISPLAY_NAME);
        container.containerName = DomTools.getChildElementPCData(node, CONTAINER_NAME);
        container.codebase = DomTools.getChildElementPCData(node, CODEBASE);
        container.className = DomTools.getChildElementPCData(node, CLASSNAME);
        container.properties = DomTools.readProperties(node);      
    }

}
