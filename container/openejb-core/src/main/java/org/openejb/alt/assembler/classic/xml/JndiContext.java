package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.JndiContextInfo;
import org.w3c.dom.Node;

public class JndiContext extends JndiContextInfo implements DomObject{

    public static final String JNDI_CONTEXT_ID = "jndi-context-id";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        jndiContextId = DomTools.getChildElementPCData(node, JNDI_CONTEXT_ID);
        properties = DomTools.readProperties(node);      
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}

