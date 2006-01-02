package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.StatefulBeanInfo;
import org.w3c.dom.Node;

public class StatefulBean extends StatefulBeanInfo implements DomObject{

    public static final String TRANSACTION_TYPE = "transaction-type";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        EnterpriseBean.initializeFromDOM(node, this);

        transactionType = DomTools.getChildElementPCData(node, TRANSACTION_TYPE);
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}

