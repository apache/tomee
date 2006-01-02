package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.MethodTransactionInfo;
import org.w3c.dom.Node;

public class MethodTransaction extends MethodTransactionInfo implements DomObject{

    public static final String DESCRIPTION = "description";

    public static final String METHOD = "method";

    public static final String TRANS_ATTRIBUTE = "trans-attribute";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        description = DomTools.getChildElementPCData(node, DESCRIPTION);
        transAttribute = DomTools.getChildElementPCData(node, TRANS_ATTRIBUTE);

        DomObject[] dos = DomTools.collectChildElementsByType(node, Method.class, METHOD);
        methods = new Method[dos.length];
        for (int i=0; i < dos.length; i++) methods[i] = (Method)dos[i];

    }

    public void serializeToDOM(Node node) throws OpenEJBException{}

}
