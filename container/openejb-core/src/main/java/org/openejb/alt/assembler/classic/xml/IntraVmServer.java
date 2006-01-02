package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.IntraVmServerInfo;
import org.w3c.dom.Node;

public class IntraVmServer extends IntraVmServerInfo implements DomObject {

    public static final String PROXY_FACTORY = "proxy-factory";

    public static final String CODEBASE = "codebase";

    public void initializeFromDOM(Node node) throws OpenEJBException{

        proxyFactoryClassName = DomTools.getChildElementPCData(node, PROXY_FACTORY);
        codebase = DomTools.getChildElementPCData(node, CODEBASE);

        proxyFactoryClass = DomTools.toolkit.forName(proxyFactoryClassName);

        properties = DomTools.readProperties(node);
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
