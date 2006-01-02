package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.EjbReferenceLocationInfo;
import org.w3c.dom.Node;

public class EjbReferenceLocation extends EjbReferenceLocationInfo implements DomObject{

    public static final String EJB_DEPLOYMENT_ID = "ejb-deployment-id";
    public static final String REMOTE_REF_NAME = "remote-ref-name";
    public static final String JNDI_CONTEXT_ID = "jndi-context-id";

    public void initializeFromDOM(Node node) throws OpenEJBException{
       ejbDeploymentId = DomTools.getChildElementPCData(node, EJB_DEPLOYMENT_ID);
       if(ejbDeploymentId==null){
            remote = true;
            remoteRefName = DomTools.getChildElementPCData(node, REMOTE_REF_NAME);
            jndiContextId = DomTools.getChildElementPCData(node, JNDI_CONTEXT_ID);
       }
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}

