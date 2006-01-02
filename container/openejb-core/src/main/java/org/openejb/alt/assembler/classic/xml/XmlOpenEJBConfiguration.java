package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.OpenEjbConfiguration;
import org.w3c.dom.Node;

public class XmlOpenEJBConfiguration extends OpenEjbConfiguration implements DomObject {

    public static final String OPENEJB = "openejb";

    public static final String CONTAINER_SYSTEM = "container-system";

    public static final String FACILITIES = "facilities";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        Node openejbElement = DomTools.getChildElement(node, OPENEJB);
        containerSystem = (ContainerSystem)DomTools.collectChildElementByType(openejbElement, ContainerSystem.class, CONTAINER_SYSTEM);
        facilities = (Facilities)DomTools.collectChildElementByType(openejbElement, Facilities.class, FACILITIES);
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
