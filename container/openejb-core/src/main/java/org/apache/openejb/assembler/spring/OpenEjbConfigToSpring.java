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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.assembler.spring;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.openejb.EnvProps;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EjbLocalReferenceInfo;
import org.apache.openejb.assembler.classic.EjbReferenceInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EntityBeanInfo;
import org.apache.openejb.assembler.classic.EnvEntryInfo;
import org.apache.openejb.assembler.classic.MethodInfo;
import org.apache.openejb.assembler.classic.MethodPermissionInfo;
import org.apache.openejb.assembler.classic.MethodTransactionInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.OpenEjbConfigurationFactory;
import org.apache.openejb.assembler.classic.QueryInfo;
import org.apache.openejb.assembler.classic.ResourceReferenceInfo;
import org.apache.openejb.assembler.classic.RoleMappingInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.spring.xmlwriter.PrettyPrintXmlWriter;
import org.apache.openejb.assembler.spring.xmlwriter.XmlWriter;

/**
 * @version $Revision$ $Date$
 */
public class OpenEjbConfigToSpring {
    private static final Map<String, String> containerTags;
    private static final Map<String, String> deploymentTags;
    static {
        Map<String, String> tags = new LinkedHashMap<String, String>();
        tags.put("org.apache.openejb.core.stateless.StatelessContainer", "statelessDeployments");
        tags.put("org.apache.openejb.core.stateful.StatefulContainer", "statefulDeployments");
        tags.put("org.apache.openejb.core.entity.EntityContainer", "bmpDeployments");
        tags.put("org.apache.openejb.alt.containers.castor_cmp11.CastorCMP11_EntityContainer", "cmpDeployments");
        containerTags = Collections.unmodifiableMap(tags);

        tags = new LinkedHashMap<String, String>();
        tags.put("org.apache.openejb.core.stateless.StatelessContainer", "o:statelessDeployment");
        tags.put("org.apache.openejb.core.stateful.StatefulContainer", "o:statefulDeployment");
        tags.put("org.apache.openejb.core.entity.EntityContainer", "o:bmpDeployment");
        tags.put("org.apache.openejb.alt.containers.castor_cmp11.CastorCMP11_EntityContainer", "o:cmpDeployment");
        deploymentTags = Collections.unmodifiableMap(tags);
    }

    public static void dump(Properties props) throws Exception {
        String className = props.getProperty(EnvProps.CONFIGURATION_FACTORY);
        if (className == null) {
            className = props.getProperty("openejb.configurator", "org.apache.openejb.alt.config.ConfigurationFactory");
        }

        Class clazz = Class.forName(className);
        OpenEjbConfigurationFactory configFactory = (OpenEjbConfigurationFactory) clazz.newInstance();
        configFactory.init(props);

        dump(configFactory, System.out);
    }

    public static void dump(OpenEjbConfigurationFactory configFactory) throws OpenEJBException {
        dump(configFactory, System.out);
    }

    public static void dump(OpenEjbConfigurationFactory configFactory, PrintStream out) throws OpenEJBException {
        OpenEjbConfigToSpring converter = new OpenEjbConfigToSpring(out);
        converter.printConf(configFactory.getOpenEjbConfiguration());
    }

    private final XmlWriter writer;
    private PrintWriter printWriter;
    private static final String ASSEMBLY_ID = "assembly";
    private static final String CLASS_LOADER_ID = "classLoader";
    private static final String TRANSACTION_MANAGER_ID = "transactionManager";

    public OpenEjbConfigToSpring(PrintStream out) {
        printWriter = new PrintWriter(out, true);
        writer = new PrettyPrintXmlWriter(printWriter);
    }

    public void printConf(OpenEjbConfiguration configInfo) {
        Map<String, String> jarPathIndex = new LinkedHashMap<String, String>();
        for (EjbJarInfo ejbJar : configInfo.containerSystem.ejbJars) {
            for (EnterpriseBeanInfo ejb : ejbJar.enterpriseBeans) {
                jarPathIndex.put(ejb.ejbDeploymentId, ejbJar.jarPath);
            }
        }

        for (ContainerInfo container : configInfo.containerSystem.containers) {
            writer.startElement("bean");
            writer.addAttribute("id", containerTags.get(container.className));
            writer.addAttribute("class", "java.util.LinkedHashMap");

            writer.startElement("constructor-arg");
            writer.addAttribute("index", "0");

            writer.startElement("map");
            for (EnterpriseBeanInfo bean : container.ejbeans) {

                writer.startElement("entry");
                writer.addAttribute("key", bean.ejbDeploymentId);

                writeEJB(container, bean, jarPathIndex.get(bean.ejbDeploymentId));

                writer.endElement();
            }
            writer.endElement();

            writer.endElement();
            writer.endElement();
        }

        writeAssembly(configInfo);

        printWriter.println();
        printWriter.flush();
    }

    private void writeEJB(ContainerInfo container, EnterpriseBeanInfo bean, String jarPath) {
        writer.startElement(deploymentTags.get(container.className));
        writer.addAttribute("id", bean.ejbDeploymentId);
        addOptionalAttribute("homeInterface", bean.home);
        addOptionalAttribute("remoteInterface", bean.remote);
        addOptionalAttribute("localHomeInterface", bean.localHome);
        addOptionalAttribute("localInterface", bean.local);
        addOptionalAttribute("beanClass", bean.ejbClass);
        writer.addAttribute("jarPath", jarPath);
        writer.addAttribute("classLoader", "#" + CLASS_LOADER_ID);
        writer.addAttribute("transactionManager", "#" + TRANSACTION_MANAGER_ID);
        writer.addAttribute("assembly", "#" + ASSEMBLY_ID);

        // session
        if (!(bean instanceof EntityBeanInfo)) {
            if ("bean".equalsIgnoreCase(bean.transactionType)) {
                writer.addAttribute("beanManagedTransaction", "true");
            }
        }

        // entity
        if (bean instanceof EntityBeanInfo) {
            EntityBeanInfo entity = (EntityBeanInfo) bean;
            writer.addAttribute("reentrant", entity.reentrant);
            writer.addAttribute("pkClass", entity.primKeyClass);

            if ("container".equalsIgnoreCase(entity.persistenceType)) {
                addOptionalAttribute("primKeyField", entity.primKeyField);

                if (entity.cmpFieldNames != null && !entity.cmpFieldNames.isEmpty()) {
                    writer.startElement("o:cmpFields");
                    writer.writeText(createCsv(entity.cmpFieldNames));
                    writer.endElement();
                }

                if (entity.queries != null && !entity.queries.isEmpty()) {
                    writer.startElement("o:queries");
                    for (QueryInfo query : entity.queries) {
                        writer.startElement("o:query");
                        writer.addAttribute("method", new MethodSignature(query.method.methodName, query.method.methodParams).toString());
                        writer.writeText(query.queryStatement);
                        writer.endElement();
                    }
                    writer.endElement();
                }
            }
        }

        writeEnc(bean);

        writer.endElement();
    }

    private void writeEnc(EnterpriseBeanInfo bean) {
        // enc
        writer.startElement("o:jndiContext");
        writer.startElement("o:enc");

        // env entries
        JndiEncInfo jndiEnc = bean.jndiEnc;
        for (EnvEntryInfo entry : jndiEnc.envEntries) {
            writer.startElement("o:envEntry");

            writer.addAttribute("name", entry.name);
            writer.addAttribute("type", entry.type);
            writer.addAttribute("value", entry.value);

            writer.endElement();
        }

        // ejb refs
        // remote ejb refs
        for (EjbReferenceInfo ejbRef : bean.jndiEnc.ejbReferences) {
            writer.startElement("o:ejbRef");

            writer.addAttribute("name", ejbRef.referenceName);
            writer.addAttribute("local", "false");

            if (!ejbRef.location.remote) {
                writer.addAttribute("ejbId", ejbRef.location.ejbDeploymentId);
            } else {
                writer.addAttribute("remoteName", ejbRef.location.jndiContextId);
                writer.addAttribute("remoteContextId", ejbRef.location.jndiContextId);
            }

            writer.endElement();
        }

        // local ejb refs
        for (EjbLocalReferenceInfo ejbRef : bean.jndiEnc.ejbLocalReferences) {
            writer.startElement("o:ejbRef");

            writer.addAttribute("name", ejbRef.referenceName);
            writer.addAttribute("local", "true");
            writer.addAttribute("ejbId", ejbRef.location.ejbDeploymentId);

            writer.endElement();
        }

        // resource refs
        for (ResourceReferenceInfo resourceRef : bean.jndiEnc.resourceRefs) {
            writer.startElement("o:resourceRef");

            writer.addAttribute("name", resourceRef.referenceName);

            if (resourceRef.location == null || !resourceRef.location.remote) {
                writer.addAttribute("resourceId", resourceRef.resourceID);
            } else {
                writer.addAttribute("remoteName", resourceRef.location.jndiContextId);
                writer.addAttribute("remoteContextId", resourceRef.location.jndiContextId);
            }

            writer.endElement();
        }

        writer.endElement();
        writer.endElement();
    }

    private void writeAssembly(OpenEjbConfiguration configInfo) {
        writer.startElement("o:assembly");
        writer.addAttribute("id", ASSEMBLY_ID);

        Map<String, List<String>> map = new TreeMap<String, List<String>>();
        for (RoleMappingInfo mapping : configInfo.facilities.securityService.roleMappings) {
            for (String logicalRoleName : mapping.logicalRoleNames) {
                if (map.containsKey(logicalRoleName)) {
                    throw new IllegalStateException("Logical role " + logicalRoleName + " is mapped to " +
                            map.get(logicalRoleName) + " and " + mapping.physicalRoleNames);
                }
                map.put(logicalRoleName, mapping.physicalRoleNames);
            }
        }

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String logicalRoleName = entry.getKey();
            List<String> physicalRoleNames = entry.getValue();
            writer.startElement("o:roleMapping");
            writer.addAttribute("logical", logicalRoleName);
            writer.addAttribute("physical", createCsv(physicalRoleNames));
            writer.endElement();
        }

        for (MethodPermissionInfo permissionInfo : configInfo.containerSystem.methodPermissions) {
            writer.startElement("o:permission");
            writer.addAttribute("roleNames", createCsv(permissionInfo.roleNames));
            writeMethodInfos(permissionInfo.methods);
            writer.endElement();
        }

        for (MethodTransactionInfo transactionInfo : configInfo.containerSystem.methodTransactions) {
            writer.startElement("o:transaction");
            writer.addAttribute("transAttribute", transactionInfo.transAttribute);
            writeMethodInfos(transactionInfo.methods);
            writer.endElement();
        }

        writer.endElement();
    }

    private void writeMethodInfos(List<MethodInfo> methods) {
        for (MethodInfo methodInfo : methods) {
            writer.startElement("o:method");
            writer.addAttribute("deploymentId", methodInfo.ejbDeploymentId.toString());
            addOptionalAttribute("intf", methodInfo.methodIntf);
            if (!"*".equals(methodInfo.methodName)) {
                writer.addAttribute("name", methodInfo.methodName);
            }
            addOptionalAttribute("params", createCsv(methodInfo.methodParams));
            writer.endElement();
        }
    }

    private void addOptionalAttribute(String key, String value) {
        if (value != null) {
            writer.addAttribute(key, value);
        }
    }

    private static String createCsv(String[] strings) {
        if (strings == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder();
        for (int k = 0; k < strings.length; k++) {
            String fieldName = strings[k];
            if (k > 0) buf.append(", ");
            buf.append(fieldName);
        }
        return buf.toString();
    }

    private static String createCsv(List<String> strings) {
        if (strings == null) {
            return null;
        }

        return createCsv(strings.toArray(new String[strings.size()]));
    }
}
