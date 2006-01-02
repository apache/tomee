package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.EntityBeanInfo;
import org.w3c.dom.Node;

public class EntityBean extends EntityBeanInfo implements DomObject{

    public static final String PERSISTENCE_TYPE = "persistence-type";

    public static final String PRIMARY_KEY_CLASS = "prim-key-class";

    public static final String PRIMARY_KEY_FIELD = "primkey-field";

    public static final String REENTRANT = "reentrant";

    public static final String CMP_FIELD_NAME = "cmp-field-name";

    public static final String QUERY = "query";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        EnterpriseBean.initializeFromDOM(node, this);

        persistenceType = DomTools.getChildElementPCData(node, PERSISTENCE_TYPE);
        primKeyClass = DomTools.getChildElementPCData(node, PRIMARY_KEY_CLASS);
        primKeyField = DomTools.getChildElementPCData(node, PRIMARY_KEY_FIELD);
        reentrant = DomTools.getChildElementPCData(node, REENTRANT);
        cmpFieldNames = DomTools.getChildElementsPCData(node, CMP_FIELD_NAME);

        DomObject[] dos = DomTools.collectChildElementsByType(node, Query.class, QUERY);
        queries = new Query[dos.length];
        for (int i=0; i < dos.length; i++) queries[i] = (Query)dos[i];

        transactionType = "Container";
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}

