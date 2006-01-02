package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.SecurityServiceInfo;
import org.w3c.dom.Node;

public class SecurityService extends SecurityServiceInfo implements DomObject{

    public static final String ROLE_MAPPING = "role-mapping";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        Service.initializeFromDOM(node, this);
        /* RoleMapping */
        DomObject[] dos = DomTools.collectChildElementsByType(node, RoleMapping.class, ROLE_MAPPING);
        roleMappings = new RoleMapping[dos.length];
        for (int i=0; i < dos.length; i++) roleMappings[i] = (RoleMapping)dos[i];
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
