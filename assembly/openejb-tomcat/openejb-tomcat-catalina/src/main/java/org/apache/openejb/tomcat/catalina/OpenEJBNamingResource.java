package org.apache.openejb.tomcat.catalina;

import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResourceEnvRef;
import org.apache.catalina.deploy.NamingResources;

/**
 * @author rmannibucau
 */
public class OpenEJBNamingResource extends NamingResources {
    public void addEnvironment(ContextEnvironment environment) {
        // tomcat uses a hastable to store entry type, null values are not allowed
        if (environment.getType() == null) {
            environment.setType("");
        }
        super.addEnvironment(environment);
    }

    @Override
    public void addResourceEnvRef(ContextResourceEnvRef ref) {
        // tomcat uses a hastable to store entry type, null values are not allowed
        if (ref.getType() == null) {
            ref.setType("");
        }
        super.addResourceEnvRef(ref);
    }
}