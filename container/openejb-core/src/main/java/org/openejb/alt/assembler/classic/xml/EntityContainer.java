package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.EnterpriseBeanInfo;
import org.openejb.alt.assembler.classic.EntityContainerInfo;
import org.w3c.dom.Node;

public class EntityContainer extends EntityContainerInfo implements DomObject{

    public static final String ENTITY_BEAN = "entity-bean";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        try{

        Container.initializeFromDOM(node, this);

        DomObject[] dos = DomTools.collectChildElementsByType(node, EntityBean.class, ENTITY_BEAN);
        beans = new EntityBean[dos.length];
        for (int i=0; i < dos.length; i++) beans[i] = (EntityBean)dos[i];
        ejbeans = (EnterpriseBeanInfo[])beans;
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
