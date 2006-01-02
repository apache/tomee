package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.ResourceReferenceInfo;
import org.w3c.dom.Node;

public class ResourceReference extends ResourceReferenceInfo implements DomObject{

    public static final String RES_REF_NAME = "res-ref-name";

    public static final String RES_REF_TYPE = "res-ref-type";

    public static final String RES_REF_AUTH = "res-ref-auth";

    public static final String RES_ID = "res-id";

    public static final String RES_REF_LOCATION = "res-ref-location";

    public void initializeFromDOM(Node node) throws OpenEJBException{

        referenceName = DomTools.getChildElementPCData(node, RES_REF_NAME);
        referenceType = DomTools.getChildElementPCData(node, RES_REF_TYPE);
        referenceAuth = DomTools.getChildElementPCData(node, RES_REF_AUTH);

        resourceID = DomTools.getChildElementPCData(node, RES_ID);

        properties = DomTools.readProperties(node);      

        location = (ResourceReferenceLocation)DomTools.collectChildElementByType(node, ResourceReferenceLocation.class, RES_REF_LOCATION);

    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
