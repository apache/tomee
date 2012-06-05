package org.superbiz.deltaspike.config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;

@ApplicationScoped
public class Counter {
    @Inject
    @ConfigProperty(name = "loop.size")
    private Integer iterations;

    public int loop() {
        for (int i = 0; i < iterations; i++) {
            System.out.println("Iteration #" + i);
        }
        return iterations;
    }
}
