/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.assembler.spring;

import org.openejb.alt.config.DeployedJar;
import org.openejb.alt.config.DeploymentLoader;
import org.openejb.alt.config.EjbJarInfoBuilder;
import org.openejb.assembler.classic.EjbJarBuilder;
import org.openejb.assembler.classic.EjbJarInfo;
import org.openejb.core.DeploymentInfo;
import org.springframework.beans.factory.FactoryBean;

import java.util.HashMap;
import java.util.List;

/**
 * @org.apache.xbean.XBean element="deployments"
 */
public class DeploymentsFactory implements FactoryBean {

    private Object value;
    private DeploymentLoader.Type type;

    public String getJar() {
        return (String) value;
    }

    public void setJar(String jar) {
        this.type = DeploymentLoader.Type.JAR;
        this.value = jar;
    }

    public String getDir() {
        return (String) value;
    }

    public void setDir(String dir) {
        this.type = DeploymentLoader.Type.DIR;
        this.value = dir;
    }

    public ClassLoader getClasspath() {
        return (ClassLoader) value;
    }

    public void setClasspath(ClassLoader classpath) {
        this.type = DeploymentLoader.Type.CLASSPATH;
        this.value = classpath;
    }

    public Object getObject() throws Exception {
        DeploymentLoader loader = new DeploymentLoader();
        List<DeployedJar> deployedJars = loader.load(type, value);

        EjbJarInfoBuilder infoBuilder = new EjbJarInfoBuilder();

        ClassLoader classLoader = (value instanceof ClassLoader) ? (ClassLoader) value : Thread.currentThread().getContextClassLoader();
        EjbJarBuilder builder = new EjbJarBuilder(classLoader);

        HashMap<String, DeploymentInfo> deployments = new HashMap();
        for (DeployedJar jar : deployedJars) {
            EjbJarInfo jarInfo = infoBuilder.buildInfo(jar);
            deployments.putAll(builder.build(jarInfo));
        }
        return null;
    }

    public Class getObjectType() {
        return null;
    }

    public boolean isSingleton() {
        return false;
    }
}
