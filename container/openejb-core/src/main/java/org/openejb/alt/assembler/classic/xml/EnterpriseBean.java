package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.EnterpriseBeanInfo;
import org.w3c.dom.Node;

public class EnterpriseBean extends EnterpriseBeanInfo{

    public static final String DESCRIPTION = "description";

    public static final String DISPLAY_NAME = "display-name";

    public static final String SMALL_ICON = "small-icon";

    public static final String LARGE_ICON = "large-icon";

    public static final String EJB_DEPLOYMENT_ID = "ejb-deployment-id";

    public static final String HOME = "home";

    public static final String REMOTE = "remote";

    public static final String EJB_CLASS = "ejb-class";

    public static final String JNDI_ENC = "jndi-enc";

    public static final String SECURITY_ROLE_REF = "security-role-ref";

    public static void initializeFromDOM(Node node, EnterpriseBeanInfo beanInfo) throws OpenEJBException{
        beanInfo.description = DomTools.getChildElementPCData(node, DESCRIPTION);
        beanInfo.displayName = DomTools.getChildElementPCData(node, DISPLAY_NAME);
        beanInfo.smallIcon = DomTools.getChildElementPCData(node, SMALL_ICON);
        beanInfo.largeIcon = DomTools.getChildElementPCData(node, LARGE_ICON);
        beanInfo.ejbDeploymentId = DomTools.getChildElementPCData(node, EJB_DEPLOYMENT_ID);
        beanInfo.home = DomTools.getChildElementPCData(node, HOME);
        beanInfo.remote = DomTools.getChildElementPCData(node, REMOTE);
        beanInfo.ejbClass = DomTools.getChildElementPCData(node, EJB_CLASS);

        /* SecurityRoleReference */
        DomObject[] dos = DomTools.collectChildElementsByType(node, SecurityRoleReference.class, SECURITY_ROLE_REF);
        beanInfo.securityRoleReferences = new SecurityRoleReference[dos.length];
        for (int i=0; i < dos.length; i++) beanInfo.securityRoleReferences[i] = (SecurityRoleReference)dos[i];

        beanInfo.jndiEnc = (JndiEnc)DomTools.collectChildElementByType(node, JndiEnc.class, JNDI_ENC);
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
