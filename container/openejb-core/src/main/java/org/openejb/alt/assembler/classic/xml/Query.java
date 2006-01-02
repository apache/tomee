package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.QueryInfo;
import org.w3c.dom.Node;

public class Query extends QueryInfo implements DomObject{

    public static final String DESCRIPTION = "description";

    public static final String QUERY_STATEMENT = "query-statement";

    public static final String METHOD = "method";

    public void initializeFromDOM(Node node) throws OpenEJBException{
        description = DomTools.getChildElementPCData(node, DESCRIPTION);
        queryStatement = DomTools.getChildElementPCData(node, QUERY_STATEMENT);

        method = (Method)DomTools.collectChildElementByType(node, Method.class, METHOD);

    }

    public void serializeToDOM(Node node) throws OpenEJBException{}

}
