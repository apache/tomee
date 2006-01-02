package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.JndiEncInfo;
import org.w3c.dom.Node;

public class JndiEnc extends JndiEncInfo implements DomObject{

    public static final String ENV_ENTRY = "env-entry";

    public static final String EJB_REF = "ejb-ref";

     public static final String RESOURCE_REF = "resource-ref";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        /* EnvEntry */
        DomObject[] dos = DomTools.collectChildElementsByType(node, EnvEntry.class, ENV_ENTRY);
        envEntries = new EnvEntry[dos.length];
        for (int i=0; i < dos.length; i++) envEntries[i] = (EnvEntry)dos[i];

        /* EjbReference */
        dos = DomTools.collectChildElementsByType(node, EjbReference.class, EJB_REF);
        ejbReferences = new EjbReference[dos.length];
        for (int i=0; i < dos.length; i++) ejbReferences[i] = (EjbReference)dos[i];

        /* ResourceRefInfo */
        dos = DomTools.collectChildElementsByType(node, ResourceReference.class, RESOURCE_REF);
        resourceRefs = new ResourceReference[dos.length];
        for (int i=0; i < dos.length; i++) resourceRefs[i] = (ResourceReference)dos[i];

    }
    public void serializeToDOM(Node node) throws OpenEJBException{}
}
