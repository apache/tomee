package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.EnterpriseBeanInfo;
import org.openejb.alt.assembler.classic.StatefulSessionContainerInfo;
import org.w3c.dom.Node;

public class StatefulSessionContainer extends StatefulSessionContainerInfo implements DomObject{

    public static final String STATEFUL_BEAN = "stateful-bean";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        try{

        Container.initializeFromDOM(node, this);

        DomObject[] dos = DomTools.collectChildElementsByType(node, StatefulBean.class, STATEFUL_BEAN);
        beans = new StatefulBean[dos.length];
        for (int i=0; i < dos.length; i++) beans[i] = (StatefulBean)dos[i];
        ejbeans = (EnterpriseBeanInfo[])beans;
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
