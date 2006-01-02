package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;

public interface DomObject {

    public void initializeFromDOM(org.w3c.dom.Node node) throws OpenEJBException;

    public void serializeToDOM(org.w3c.dom.Node node) throws OpenEJBException;

}
