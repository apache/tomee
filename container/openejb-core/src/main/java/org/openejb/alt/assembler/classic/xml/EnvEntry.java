package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.EnvEntryInfo;
import org.w3c.dom.Node;

public class EnvEntry extends EnvEntryInfo implements DomObject{

    public static final String ENV_ENTRY_NAME = "env-entry-name";

    public static final String ENV_ENTRY_TYPE = "env-entry-type";

    public static final String ENV_ENTRY_VALUE = "env-entry-value";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        name = DomTools.getChildElementPCData(node, ENV_ENTRY_NAME);
        type = DomTools.getChildElementPCData(node, ENV_ENTRY_TYPE);
        value = DomTools.getChildElementPCData(node, ENV_ENTRY_VALUE);
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
