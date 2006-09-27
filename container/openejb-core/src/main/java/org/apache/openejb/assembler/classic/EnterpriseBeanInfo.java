package org.apache.openejb.assembler.classic;

import java.util.List;
import java.util.ArrayList;

public abstract class EnterpriseBeanInfo extends InfoObject {

    public static final int ENTITY = 0;

    public static final int STATEFUL = 1;

    public static final int STATELESS = 2;

    public int type;

    public String codebase;
    public String description;
    public String displayName;
    public String smallIcon;
    public String largeIcon;
    public String ejbDeploymentId;
    public String home;
    public String remote;
    public String localHome;
    public String local;
    public String businessLocal;
    public String businessRemote;
    public String ejbClass;
    public String ejbName;

    public String transactionType;
    public JndiEncInfo jndiEnc;
    public SecurityRoleReferenceInfo [] securityRoleReferences;

    public List<LifecycleCallbackInfo> postConstruct = new ArrayList();
    public List<LifecycleCallbackInfo> preDestroy = new ArrayList();

}
