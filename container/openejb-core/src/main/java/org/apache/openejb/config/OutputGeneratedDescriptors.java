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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.JAXBContextFactory;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.JpaJaxbUtil;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.oejb2.GeronimoEjbJarType;
import org.apache.openejb.jee.oejb2.JaxbOpenejbJar2;
import org.apache.openejb.jee.oejb3.JaxbOpenejbJar3;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.sxc.EjbJarXml;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class OutputGeneratedDescriptors implements DynamicDeployer {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, "org.apache.openejb.util.resources");
    public static final String OUTPUT_DESCRIPTORS = "openejb.descriptors.output";
    public static final String OUTPUT_DESCRIPTORS_FOLDER = "openejb.descriptors.output.folder";

    public AppModule deploy(final AppModule appModule) throws OpenEJBException {
        boolean output = SystemInstance.get().getOptions().get(OUTPUT_DESCRIPTORS, false);

        if (output && appModule.getCmpMappings() != null) {

            writeGenratedCmpMappings(appModule);
        }

        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            final Options options = new Options(ejbModule.getOpenejbJar().getProperties(), SystemInstance.get().getOptions());

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

        for (final ConnectorModule connectorModule : appModule.getConnectorModules()) {
            writeRaXml(connectorModule);
        }

        return appModule;
    }

    private void writeRaXml(final ConnectorModule connectorModule) {
        try {
            final Connector connector = connectorModule.getConnector();

            final File tempFile = tempFile("ra-", connectorModule.getModuleId() + ".xml");

            final OutputStream out = IO.write(tempFile);
            try {
                final JAXBContext ctx = JAXBContextFactory.newInstance(Connector.class);
                final Marshaller marshaller = ctx.createMarshaller();
                marshaller.marshal(connector, out);
                logger.info("Dumping Generated ra.xml to: " + tempFile.getAbsolutePath());
            } catch (final JAXBException e) {
                // no-op
            } finally {
                IO.close(out);
            }
        } catch (final IOException e) {
            // no-op
        }
    }

    private File tempFile(final String start, final String end) throws IOException {
        if (SystemInstance.get().getOptions().get(OUTPUT_DESCRIPTORS_FOLDER, (String) null) != null) {
            final File tmp = new File(SystemInstance.get().getOptions().get(OUTPUT_DESCRIPTORS_FOLDER, ""));
            if (!tmp.exists()) {
                if (!tmp.mkdirs()) {
                    throw new IOException("can't create " + tmp.getAbsolutePath());
                }
            }
            return new File(tmp, start + Long.toString(new Random().nextInt()) + end);
        } else {
            try {
                return File.createTempFile(start, end);
            } catch (final Throwable e) {

                final File tmp = new File("tmp");
                if (!tmp.exists() && !tmp.mkdirs()) {
                    throw new IOException("Failed to create local tmp directory: " + tmp.getAbsolutePath());
                }

                return File.createTempFile(start, end, tmp);
            }
        }
    }

    private void writeGenratedCmpMappings(final AppModule appModule) {

        for (final PersistenceModule persistenceModule : appModule.getPersistenceModules()) {
            try {
                final Persistence persistence = persistenceModule.getPersistence();
                if (hasCmpPersistenceUnit(persistence)) {
                    final File tempFile = tempFile("persistence-", ".xml");
                    final OutputStream out = IO.write(tempFile);
                    try {
                        JpaJaxbUtil.marshal(Persistence.class, persistence, out);
                        logger.info("Dumping Generated CMP persistence.xml to: " + tempFile.getAbsolutePath());
                    } catch (final JAXBException e) {
                        // no-op
                    } finally {
                        IO.close(out);
                    }
                }
            } catch (final IOException e) {
                // no-op
            }
        }
        try {
            final File tempFile = tempFile("openejb-cmp-generated-orm-", ".xml");
            final OutputStream out = IO.write(tempFile);
            try {
                JpaJaxbUtil.marshal(EntityMappings.class, appModule.getCmpMappings(), out);
                logger.info("Dumping Generated CMP mappings.xml to: " + tempFile.getAbsolutePath());
            } catch (final JAXBException e) {
                // no-op
            } finally {
                IO.close(out);
            }
        } catch (final IOException e) {
            // no-op
        }
    }

    private boolean hasCmpPersistenceUnit(final Persistence persistence) {
        for (final PersistenceUnit unit : persistence.getPersistenceUnit()) {
            if (unit.getName().startsWith("cmp")) {
                return true;
            }
        }
        return false;
    }

    private void writeOpenejbJar(final EjbModule ejbModule) {
        try {
            final OpenejbJar openejbJar = ejbModule.getOpenejbJar();
            final File tempFile = tempFile("openejb-jar-", ejbModule.getModuleId() + ".xml");

            final OutputStream out = IO.write(tempFile);
            try {
                JaxbOpenejbJar3.marshal(OpenejbJar.class, openejbJar, out);
                logger.info("Dumping Generated openejb-jar.xml to: " + tempFile.getAbsolutePath());
            } catch (final JAXBException e) {
                // no-op
            } finally {
                IO.close(out);
            }
        } catch (final Exception e) {
            // no-op
        }
    }

    private void writeGeronimoOpenejb(final EjbModule ejbModule) {
        try {
            final GeronimoEjbJarType geronimoEjbJarType = (GeronimoEjbJarType) ejbModule.getAltDDs().get("geronimo-openejb.xml");

            if (geronimoEjbJarType == null) {
                return;
            }

            final File tempFile = tempFile("geronimo-openejb-", ejbModule.getModuleId() + ".xml");

            final OutputStream out = IO.write(tempFile);
            try {
                JaxbOpenejbJar2.marshal(GeronimoEjbJarType.class, geronimoEjbJarType, out);
                logger.info("Dumping Generated geronimo-openejb.xml to: " + tempFile.getAbsolutePath());
            } catch (final JAXBException e) {
                // no-op
            } finally {
                IO.close(out);
            }
        } catch (final Exception e) {
            // no-op
        }
    }

    private void writeEjbJar(final EjbModule ejbModule) {
        try {
            final EjbJar ejbJar = ejbModule.getEjbJar();
            final File tempFile = tempFile("ejb-jar-", ejbModule.getModuleId() + ".xml");

            final OutputStream out = IO.write(tempFile);
            try {
                EjbJarXml.marshal(ejbJar, out);
                logger.info("Dumping Generated ejb-jar.xml to: " + tempFile.getAbsolutePath());
            } catch (final JAXBException e) {
                // no-op
            } finally {
                IO.close(out);
            }
        } catch (final Exception e) {
            // no-op
        }
    }
}
