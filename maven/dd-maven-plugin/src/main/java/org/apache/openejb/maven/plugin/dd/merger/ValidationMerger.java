/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
