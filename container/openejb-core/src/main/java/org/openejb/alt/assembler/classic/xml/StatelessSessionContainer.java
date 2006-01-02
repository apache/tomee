package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.EnterpriseBeanInfo;
import org.openejb.alt.assembler.classic.StatelessSessionContainerInfo;
import org.w3c.dom.Node;

public class StatelessSessionContainer extends StatelessSessionContainerInfo implements DomObject{

    public static final String STATELESS_BEAN = "stateless-bean";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        Container.initializeFromDOM(node, this);;

        DomObject[] dos = DomTools.collectChildElementsByType(node, StatelessBean.class, STATELESS_BEAN);
        beans = new StatelessBean[dos.length];
        for (int i=0; i < dos.length; i++) beans[i] = (StatelessBean)dos[i];
        ejbeans = (EnterpriseBeanInfo[])beans;

    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
