package org.superbiz.cdi.stereotype;

import javax.ejb.Singleton;
import javax.inject.Inject;

@Singleton
public class Journey {
    @Inject private Vehicle vehicle;
    @Inject private Society society;

    public String vehicle() {
        return vehicle.name();
    }

    public String category() {
        return society.category();
    }
}
