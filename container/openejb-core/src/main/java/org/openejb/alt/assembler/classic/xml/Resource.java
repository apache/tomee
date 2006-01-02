package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.ResourceInfo;
import org.w3c.dom.Node;

public class Resource extends ResourceInfo implements DomObject{

    public static final String RES_ID = "res-id";

    public void initializeFromDOM(Node node) throws OpenEJBException{

        resourceID = DomTools.getChildElementPCData(node, RES_ID);

        properties = DomTools.readProperties(node);      

    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
