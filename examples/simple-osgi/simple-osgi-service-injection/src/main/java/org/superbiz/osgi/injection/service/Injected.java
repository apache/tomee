package org.superbiz.osgi.injection.service;

import org.osgi.service.startlevel.StartLevel;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

@Singleton
@Startup
public class Injected {
    @Inject
    private StartLevel sl;

    @PostConstruct
    public void init() {
        System.out.println();
        if (sl != null) {
            System.out.println("start level is " + sl.getStartLevel());
        } else {
            System.out.println("start level is null -> FAILED");
        }
    }
}
