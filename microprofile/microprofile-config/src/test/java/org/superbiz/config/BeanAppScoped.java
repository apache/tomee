package org.superbiz.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import javax.enterprise.context.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;

import javax.annotation.PostConstruct;

@ApplicationScoped
public class BeanAppScoped {

    @PostConstruct
    public void postConstruct() {
        System.out.println("BeanAppScoped created");
    }

    @Inject
    @ConfigProperty(name="my.string.value", defaultValue = "nothing")
    private String myString;
}
