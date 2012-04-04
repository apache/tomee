/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.apache.openejb.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.table.Line;
import org.apache.openejb.table.Lines;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;

/**
 * highly inspired from openejb command helper but with some different data.
 *
 * @goal list
 */
public class ListEjbMojo extends AbstractCommandMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Deployer deployer = (Deployer) lookup("openejb/DeployerBusinessRemote");
        final Collection<AppInfo> infos = deployer.getDeployedApps();
        final Lines lines = new Lines();
        lines.add(new Line("Name", "Class", "Interface Type", "Bean Type"));
        for (AppInfo info : infos) {
            for (EjbJarInfo ejbJar : info.ejbJars) {
                for (EnterpriseBeanInfo bean : ejbJar.enterpriseBeans) {
                    lines.add(new Line(bean.ejbDeploymentId, bean.ejbClass, getType(bean), componentType(bean)));
                }
            }
        }
        lines.print(new LogPrinterStream(getLog()));
    }

    private static String componentType(final EnterpriseBeanInfo bean) {
        return bean.getClass().getSimpleName().replace("Info", "").replace("Bean", " Bean");
    }

    private static String getType(final EnterpriseBeanInfo bc) {
        boolean empty = true;
        final StringBuilder sb = new StringBuilder();
        if (bc.localbean) {
            sb.append("Local Bean");
            empty = false;
        }
        if (bc.businessLocal != null && !bc.businessLocal.isEmpty()) {
            if (!empty) {
                sb.append(", ");
            }
            sb.append("Local").append(Arrays.asList(bc.businessLocal));
            empty = false;
        }
        if (bc.businessRemote != null && !bc.businessRemote.isEmpty()) {
            if (!empty) {
                sb.append(", ");
            }
            sb.append("Remote").append(Arrays.asList(bc.businessRemote));
        }
        return sb.toString();
    }

    private static class LogPrinterStream extends PrintStream {
        private Log logger;

        public LogPrinterStream(Log log) {
            super(new NullOuputStream());
            logger = log;
        }

        @Override
        public void print(String s) {
            logger.info(s.replace(System.getProperty("line.separator"), ""));
        }

        private static class NullOuputStream extends OutputStream {
            @Override
            public void write(int b) throws IOException {
                // no-op
            }
        }
    }
}
