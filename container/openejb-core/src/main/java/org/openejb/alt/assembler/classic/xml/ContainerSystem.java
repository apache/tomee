package org.openejb.alt.assembler.classic.xml;

import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.ContainerInfo;
import org.openejb.alt.assembler.classic.ContainerSystemInfo;
import org.w3c.dom.Node;

public class ContainerSystem extends ContainerSystemInfo implements DomObject{

    public static final String CONTAINERS = "containers";

    public static final String ENTITY_CONTAINER = "entity-container";

    public static final String CMP_ENTITY_CONTAINER = "cmp-entity-container";

    public static final String STATEFUL_SESSION_CONTAINER = "stateful-session-container";

    public static final String STATELESS_SESSION_CONTAINER = "stateless-session-container";

    public static final String SECURITY_ROLE = "security-role";

    public static final String METHOD_PERMISSION = "method-permission";

    public static final String METHOD_TRANSACTION = "method-transaction";

    public void initializeFromDOM(Node node) throws OpenEJBException{

        Node containersElement = DomTools.getChildElement(node, CONTAINERS);

        /* EntityContainer */
        DomObject[] dos = DomTools.collectChildElementsByType(containersElement, EntityContainer.class, ENTITY_CONTAINER);
        entityContainers = new EntityContainer[dos.length];
        for (int i=0; i < dos.length; i++) entityContainers[i] = (EntityContainer)dos[i];

        /* StatelessSessionContainer */
        dos = DomTools.collectChildElementsByType(containersElement, StatelessSessionContainer.class, STATELESS_SESSION_CONTAINER);
        statelessContainers = new StatelessSessionContainer[dos.length];
        for (int i=0; i < dos.length; i++) statelessContainers[i] = (StatelessSessionContainer)dos[i];

        /* StatefulSessionContainer */
        dos = DomTools.collectChildElementsByType(containersElement, StatefulSessionContainer.class, STATEFUL_SESSION_CONTAINER);
        statefulContainers = new StatefulSessionContainer[dos.length];
        for (int i=0; i < dos.length; i++) statefulContainers[i] = (StatefulSessionContainer)dos[i];

        int x=0;
        containers = new ContainerInfo[entityContainers.length +
                                       statelessContainers.length +
                                       statefulContainers.length];

        System.arraycopy(entityContainers      , 0, containers, x                            ,entityContainers.length);

        System.arraycopy(statelessContainers, 0, containers, x += entityContainers.length    ,statelessContainers.length);
        System.arraycopy(statefulContainers , 0, containers, x += statelessContainers.length ,statefulContainers.length);

        dos = DomTools.collectChildElementsByType(node, SecurityRole.class, SECURITY_ROLE);
        securityRoles = new SecurityRole[dos.length];
        for (int i=0; i < dos.length; i++) securityRoles[i] = (SecurityRole)dos[i];

        dos = DomTools.collectChildElementsByType(node, MethodPermission.class, METHOD_PERMISSION);
        methodPermissions = new MethodPermission[dos.length];
        for (int i=0; i < dos.length; i++) methodPermissions[i] = (MethodPermission)dos[i];

        dos = DomTools.collectChildElementsByType(node, MethodTransaction.class, METHOD_TRANSACTION);
        methodTransactions = new MethodTransaction[dos.length];
        for (int i=0; i < dos.length; i++) methodTransactions[i] = (MethodTransaction)dos[i];
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}

