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
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.oejb3.JaxbOpenejbJar3;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.xml.bind.JAXBException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * @version $Rev$ $Date$
 */
public class OutputGeneratedDescriptors implements DynamicDeployer {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, "org.apache.openejb.util.resources");
    private static final String OUTPUT_DESCRIPTORS = "openejb.descriptors.output";

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        boolean output = SystemInstance.get().getProperty(OUTPUT_DESCRIPTORS, "false").equalsIgnoreCase("true");

        for (EjbModule ejbModule : appModule.getEjbModules()) {

            output = ejbModule.getOpenejbJar().getProperties().getProperty(OUTPUT_DESCRIPTORS, output+"").equalsIgnoreCase("true");

            if (output){
                if (ejbModule.getEjbJar() != null) {
                    writeEjbJar(ejbModule);
                }

                if (ejbModule.getOpenejbJar() != null) {
                    writeOpenejbJar(ejbModule);
                }
            }
        }

        return appModule;
    }

    private void writeOpenejbJar(EjbModule ejbModule) {
        try {
            OpenejbJar openejbJar = ejbModule.getOpenejbJar();
            File tempFile = File.createTempFile("openejb-jar-", ejbModule.getModuleId() + ".xml");
            FileOutputStream fout = new FileOutputStream(tempFile);
            BufferedOutputStream out = new BufferedOutputStream(fout);
            try {
                JaxbOpenejbJar3.marshal(OpenejbJar.class, openejbJar, out);
                logger.info("Dumping Generated openejb-jar.xml to: " + tempFile.getAbsolutePath());
            } catch (JAXBException e) {
            } finally {
                out.close();
            }
        } catch (Exception e) {
        }
    }

    private void writeEjbJar(EjbModule ejbModule) {
        try {
            EjbJar ejbJar = ejbModule.getEjbJar();
            File tempFile = File.createTempFile("ejb-jar-", ejbModule.getModuleId() + ".xml");
            FileOutputStream fout = new FileOutputStream(tempFile);
            BufferedOutputStream out = new BufferedOutputStream(fout);
            try {
                JaxbJavaee.marshal(EjbJar.class, ejbJar, out);
                logger.info("Dumping Generated ejb-jar.xml to: " + tempFile.getAbsolutePath());
            } catch (JAXBException e) {
            } finally {
                out.close();
            }
        } catch (Exception e) {
        }
    }
}
