package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.ServiceInfo;
import org.w3c.dom.Node;

public class Service extends ServiceInfo{

    public static final String DESCRIPTION = "description";

    public static final String DISPLAY_NAME = "display-name";

    public static final String SERVICE_NAME = "service-name";

    public static final String FACTORY_CLASS = "factory-class";

    public static final String CODEBASE = "codebase";

    public static void initializeFromDOM(Node node, ServiceInfo serviceInfo) throws OpenEJBException{

        serviceInfo.description = DomTools.getChildElementPCData(node, DESCRIPTION);
        serviceInfo.displayName = DomTools.getChildElementPCData(node, DISPLAY_NAME);
        serviceInfo.serviceName = DomTools.getChildElementPCData(node, SERVICE_NAME);
        serviceInfo.factoryClassName = DomTools.getChildElementPCData(node, FACTORY_CLASS);
        serviceInfo.codebase = DomTools.getChildElementPCData(node, CODEBASE);

        serviceInfo.factoryClass = DomTools.toolkit.forName(serviceInfo.factoryClassName);

        serviceInfo.properties = DomTools.readProperties(node);

    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
