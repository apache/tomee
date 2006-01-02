package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.EjbReferenceInfo;
import org.w3c.dom.Node;

public class EjbReference extends EjbReferenceInfo implements DomObject{

    public static final String EJB_REF_NAME = "ejb-ref-name";

    public static final String HOME = "home";

    public static final String EJB_REF_LOCATION = "ejb-ref-location";

    public void initializeFromDOM(Node node) throws OpenEJBException{

        referenceName = DomTools.getChildElementPCData(node, EJB_REF_NAME);
        homeType = DomTools.getChildElementPCData(node, HOME);

        location = (EjbReferenceLocation)DomTools.collectChildElementByType(node, EjbReferenceLocation.class, EJB_REF_LOCATION);
        int i = 0;
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
