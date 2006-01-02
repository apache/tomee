package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.ResourceReferenceLocationInfo;
import org.w3c.dom.Node;

public class ResourceReferenceLocation extends ResourceReferenceLocationInfo implements DomObject{

    public static final String REMOTE_REF_NAME = "remote-ref-name";
    public static final String JNDI_CONTEXT_ID = "jndi-context-id";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        remote = true;
        remoteRefName = DomTools.getChildElementPCData(node, REMOTE_REF_NAME);
        jndiContextId = DomTools.getChildElementPCData(node, JNDI_CONTEXT_ID);
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}

