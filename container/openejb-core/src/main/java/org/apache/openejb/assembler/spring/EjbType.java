package org.apache.openejb.assembler.spring;

import org.apache.openejb.DeploymentInfo;

public class EjbType {
    public static final EjbType STATEFUL = new EjbType(DeploymentInfo.STATEFUL, "Stateful SessionBean");
    public static final EjbType STATELESS = new EjbType(DeploymentInfo.STATELESS, "Stateless SessionBean");
    public static final EjbType CMP_ENTITY = new EjbType(DeploymentInfo.CMP_ENTITY, "CMP EntityBean");
    public static final EjbType BMP_ENTITY = new EjbType(DeploymentInfo.BMP_ENTITY, "BMP EntityBean");

    private final boolean isSession;
    private final boolean isEntity;
    private final byte type;
    private final String typeName;

    private EjbType(byte type, String typeName) {
        this.type = type;
        this.typeName = typeName;
        isSession = org.apache.openejb.core.CoreDeploymentInfo.STATEFUL == type || org.apache.openejb.core.CoreDeploymentInfo.STATELESS == type;
        isEntity = !isSession;
    }

    public boolean isSession() {
        return isSession;
    }

    public boolean isEntity() {
        return isEntity;
    }

    public byte getType() {
        return type;
    }

    public String getTypeName() {
        return typeName;
    }
}
