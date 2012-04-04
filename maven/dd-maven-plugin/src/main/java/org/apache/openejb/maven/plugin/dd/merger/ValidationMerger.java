package org.apache.openejb.maven.plugin.dd.merger;

import org.apache.maven.plugin.logging.Log;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.jee.bval.PropertyType;
import org.apache.openejb.jee.bval.ValidationConfigType;
import org.apache.openejb.maven.plugin.dd.Merger;

import javax.xml.bind.JAXBElement;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

public class ValidationMerger extends Merger<ValidationConfigType> {
    public ValidationMerger(final Log logger) {
        super(logger);
    }

    @Override
    public ValidationConfigType merge(final ValidationConfigType reference, final ValidationConfigType toMerge) {
        for (PropertyType property : toMerge.getProperty()) {
            boolean found = false;
            for (PropertyType refProperty : reference.getProperty()) {
                if (refProperty.getName().contains(property.getName())) {
                    found = true;
                    break;
                }
            }
            if (found) {
                log.warn("property " + property.getName() + " already present");
            } else {
                reference.getProperty().add(property);
            }
        }

        for (JAXBElement<String> elt : toMerge.getConstraintMapping()) {
            reference.getConstraintMapping().add(elt);
        }

        return reference;
    }

    @Override
    public ValidationConfigType createEmpty() {
        return new ValidationConfigType();
    }

    @Override
    public ValidationConfigType read(URL url) {
        try {
            return JaxbOpenejb.unmarshal(ValidationConfigType.class, new BufferedInputStream(url.openStream()));
        } catch (Exception e) {
            return createEmpty();
        }
    }

    @Override
    public String descriptorName() {
        return "validation.xml";
    }

    @Override
    public void dump(File dump, ValidationConfigType object) throws Exception {
        final BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(dump));
        try {
            JaxbOpenejb.marshal(ValidationConfigType.class, object, stream);
        } finally {
            stream.close();
        }
    }
}
