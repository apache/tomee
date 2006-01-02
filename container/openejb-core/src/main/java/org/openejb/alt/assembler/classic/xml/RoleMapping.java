package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.RoleMappingInfo;
import org.w3c.dom.Node;

public class RoleMapping extends RoleMappingInfo implements DomObject{

    public static final String LOGICAL_ROLE_NAME = "logical-role-name";

    public static final String PHYSICAL_ROLE_NAME = "physical-role-name";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        logicalRoleNames = DomTools.getChildElementsPCData(node, LOGICAL_ROLE_NAME);
        physicalRoleNames = DomTools.getChildElementsPCData(node, PHYSICAL_ROLE_NAME);
    }
    public void serializeToDOM(Node node) throws OpenEJBException{}
}
