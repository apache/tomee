package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.SecurityRoleReferenceInfo;
import org.w3c.dom.Node;

public class SecurityRoleReference extends SecurityRoleReferenceInfo implements DomObject{

    public static final String DESCRIPTION = "description";

    public static final String ROLE_NAME = "role-name";

    public static final String ROLE_LINK = "role-link";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        description = DomTools.getChildElementPCData(node, DESCRIPTION);
        roleName = DomTools.getChildElementPCData(node, ROLE_NAME);
        roleLink = DomTools.getChildElementPCData(node, ROLE_LINK);
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
