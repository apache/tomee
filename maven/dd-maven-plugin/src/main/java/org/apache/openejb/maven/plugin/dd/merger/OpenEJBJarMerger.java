package org.apache.openejb.maven.plugin.dd.merger;

import org.apache.maven.plugin.logging.Log;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.JaxbOpenejbJar3;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.maven.plugin.dd.Merger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

public class OpenEJBJarMerger extends Merger<OpenejbJar> {
    public OpenEJBJarMerger(final Log logger) {
        super(logger);
    }

    @Override
    public OpenejbJar merge(OpenejbJar reference, OpenejbJar toMerge) {
        new EnvEntriesMerger(log).merge(reference.getProperties(), toMerge.getProperties());

        for (EjbDeployment deployment : toMerge.getEjbDeployment()) {
            if (reference.getDeploymentsByEjbName().containsKey(deployment.getEjbName())) {
                log.warn("ejb deployement " + deployment.getEjbName() + " already present");
            } else {
                reference.addEjbDeployment(deployment);
            }
        }

        return reference;
    }

    @Override
    public OpenejbJar createEmpty() {
        return new OpenejbJar();
    }

    @Override
    public OpenejbJar read(URL url) {
        try {
            return JaxbOpenejbJar3.unmarshal(OpenejbJar.class, new BufferedInputStream(url.openStream()));
        } catch (Exception e) {
            return createEmpty();
        }
    }

    @Override
    public String descriptorName() {
        return "openejb-jar.xml";
    }

    @Override
    public void dump(File dump, OpenejbJar object) throws Exception {
        final BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(dump));
        try {
            JaxbOpenejbJar3.marshal(OpenejbJar.class, object, stream);
        } finally {
            stream.close();
        }
    }
}
