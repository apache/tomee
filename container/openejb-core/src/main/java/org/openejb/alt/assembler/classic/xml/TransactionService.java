package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.TransactionServiceInfo;
import org.w3c.dom.Node;

public class TransactionService extends TransactionServiceInfo implements DomObject{

    public void initializeFromDOM(Node node) throws OpenEJBException{
        Service.initializeFromDOM(node, this);
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}

}
