package org.openejb.alt.assembler.classic;

public class SecurityServiceInfo extends ServiceInfo {

    public RoleMappingInfo[] roleMappings;

    public SecurityServiceInfo() {
        serviceType = SECURITY_SERVICE;
    }

}
