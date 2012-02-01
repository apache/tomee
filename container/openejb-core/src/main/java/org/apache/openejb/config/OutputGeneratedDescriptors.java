/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.JAXBContextFactory;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.JpaJaxbUtil;
import org.apache.openejb.jee.oejb2.GeronimoEjbJarType;
import org.apache.openejb.jee.oejb2.JaxbOpenejbJar2;
import org.apache.openejb.jee.oejb3.JaxbOpenejbJar3;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class OutputGeneratedDescriptors implements DynamicDeployer {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, "org.apache.openejb.util.resources");
    public static final String OUTPUT_DESCRIPTORS = "openejb.descriptors.output";
    public static final String OUTPUT_DESCRIPTORS_FOLDER = "openejb.descriptors.output.folder";

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        boolean output = SystemInstance.get().getOptions().get(OUTPUT_DESCRIPTORS, false);

        if (output && appModule.getCmpMappings() != null){

            writeGenratedCmpMappings(appModule);
        }

        for (EjbModule ejbModule : appModule.getEjbModules()) {
            Options options = new Options(ejbModule.getOpenejbJar().getProperties(), SystemInstance.get().getOptions());

            final ValidationContext context = ejbModule.getValidation();

            // output descriptors by default if there are validation errors
            final boolean invalid = context.hasErrors() || context.hasFailures();

            output = options.get(OUTPUT_DESCRIPTORS, invalid);

            if (output) {
                if (ejbModule.getEjbJar() != null) {
                    writeEjbJar(ejbModule);
                }

                if (ejbModule.getOpenejbJar() != null) {
                    writeOpenejbJar(ejbModule);
                }

                writeGeronimoOpenejb(ejbModule);
            }
        }
        
        for (ConnectorModule connectorModule : appModule.getConnectorModules()) {
			writeRaXml(connectorModule);
		}

        return appModule;
    }

    private void writeRaXml(ConnectorModule connectorModule) {
    	try {
	    	Connector connector = connectorModule.getConnector();
	
	        File tempFile = tempFile("ra-", connectorModule.getModuleId() + ".xml");
	        FileOutputStream fout = new FileOutputStream(tempFile);
	        BufferedOutputStream out = new BufferedOutputStream(fout);
	
	        try {
		    	JAXBContext ctx = JAXBContextFactory.newInstance(Connector.class);
		    	Marshaller marshaller = ctx.createMarshaller();
		    	marshaller.marshal(connector, out);
	        } catch (JAXBException e) {
	        } finally {
	        	out.close();
	        }
    	} catch (IOException e) {
    	}
	}

    private File tempFile(String start, String end) throws IOException {
        if (System.getProperty(OUTPUT_DESCRIPTORS_FOLDER) != null) {
            File tmp = new File(System.getProperty(OUTPUT_DESCRIPTORS_FOLDER));
            if (!tmp.exists()) {
                if (!tmp.mkdirs()) {
                    throw new IOException("can't create " + tmp.getAbsolutePath());
                }
            }
            return new File(tmp, start + Long.toString(RandomUtils.nextInt()) + end);
        } else {
            return File.createTempFile(start, end);
        }
    }

	private void writeGenratedCmpMappings(AppModule appModule) {
        try {
            File tempFile = tempFile("openejb-cmp-generated-orm-", ".xml");
            FileOutputStream fout = new FileOutputStream(tempFile);
            BufferedOutputStream out = new BufferedOutputStream(fout);

            try {
                JpaJaxbUtil.marshal(EntityMappings.class, appModule.getCmpMappings(), out);
            } catch (JAXBException e) {
            } finally{
                out.close();
            }
        } catch (IOException e) {
        }
    }

    private void writeOpenejbJar(EjbModule ejbModule) {
        try {
            OpenejbJar openejbJar = ejbModule.getOpenejbJar();
            File tempFile = tempFile("openejb-jar-", ejbModule.getModuleId() + ".xml");
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

    private void writeGeronimoOpenejb(EjbModule ejbModule) {
        try {
            GeronimoEjbJarType geronimoEjbJarType = (GeronimoEjbJarType) ejbModule.getAltDDs().get("geronimo-openejb.xml");

            if (geronimoEjbJarType == null) return;

            File tempFile = tempFile("geronimo-openejb-", ejbModule.getModuleId() + ".xml");
            FileOutputStream fout = new FileOutputStream(tempFile);
            BufferedOutputStream out = new BufferedOutputStream(fout);
            try {
                JaxbOpenejbJar2.marshal(GeronimoEjbJarType.class, geronimoEjbJarType, out);
                logger.info("Dumping Generated geronimo-openejb.xml to: " + tempFile.getAbsolutePath());
            } catch (JAXBException e) {
            } finally {
                out.close();
            }
        } catch (Exception e) {
        }
    }

    private void writeEjbJar(EjbModule ejbModule) {
        try {
            final File tempFile = tempFile("ejb-jar-", ejbModule.getModuleId() + ".xml");
            writeEjbJar(tempFile, ejbModule);
            logger.info("Dumping Generated ejb-jar.xml to: " + tempFile.getAbsolutePath());
        } catch (IOException e) {
            // no-op
        }
    }

    public static void writeEjbJar(final File output, final EjbModule ejbModule) {
        try {
            EjbJar ejbJar = ejbModule.getEjbJar();
            FileOutputStream fout = new FileOutputStream(output);
            BufferedOutputStream out = new BufferedOutputStream(fout);
            try {
                JaxbJavaee.marshal(EjbJar.class, ejbJar, out);
            } catch (JAXBException ignored) {
                // no-op
            } finally {
                out.close();
            }
        } catch (Exception e) {
            // no-op
        }
    }
}
