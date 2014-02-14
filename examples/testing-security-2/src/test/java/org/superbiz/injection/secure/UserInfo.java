package org.superbiz.injection.secure;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

@Stateless
public class UserInfo {
    @Resource
    private SessionContext sessionContext;

    public String getUserName() {
        return sessionContext.getCallerPrincipal().getName();
    }

    public boolean isCallerInRole(String role) {
        return sessionContext.isCallerInRole(role);
    }
}
