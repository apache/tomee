package org.superbiz.cdi.ejbcontext;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;

@Stateless
public class CdiEjb {
    @Resource
    private EJBContext context;

    public String info() {
        return context.getCallerPrincipal().getName();
    }
}
