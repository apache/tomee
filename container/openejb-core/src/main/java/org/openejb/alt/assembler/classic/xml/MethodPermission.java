package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.MethodPermissionInfo;
import org.w3c.dom.Node;

public class MethodPermission extends MethodPermissionInfo implements DomObject{

    public static final String DESCRIPTION = "description";

    public static final String ROLE_NAME = "role-name";

    public static final String METHOD = "method";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        description = DomTools.getChildElementPCData(node, DESCRIPTION);
        roleNames = DomTools.getChildElementsPCData(node, ROLE_NAME);

        DomObject[] dos = DomTools.collectChildElementsByType(node, Method.class, METHOD);
        methods = new Method[dos.length];
        for (int i=0; i < dos.length; i++) methods[i] = (Method)dos[i];

    }

    public void serializeToDOM(Node node) throws OpenEJBException{}

}
