package org.superbiz.cdi.ejbcontext;

import javax.inject.Inject;

public class CdiBean {
    @Inject
    private CdiEjb ejb;

    public String info() {
        return ejb.info();
    }
}
