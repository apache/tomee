package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.SecurityRoleInfo;
import org.w3c.dom.Node;

public class SecurityRole extends SecurityRoleInfo implements DomObject{

    public static final String DESCRIPTION = "description";

    public static final String ROLE_NAME = "role-name";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        description = DomTools.getChildElementPCData(node, DESCRIPTION);
        roleName = DomTools.getChildElementPCData(node, ROLE_NAME);
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
