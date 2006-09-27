/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2006 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.apache.openejb.assembler.spring;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
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
            for (int j = 0; j < container.ejbeans.length; j++) {
                EnterpriseBeanInfo bean = container.ejbeans[j];

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

                if (entity.cmpFieldNames != null && entity.cmpFieldNames.length > 0) {
                    writer.startElement("o:cmpFields");
                    writer.writeText(createCsv(entity.cmpFieldNames));
                    writer.endElement();
                }

                if (entity.queries != null && entity.queries.length > 0) {
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
        List<EnvEntryInfo> envEntries = AssemblerUtil.asList(bean.jndiEnc.envEntries);
        if (!envEntries.isEmpty()) {
            for (EnvEntryInfo entry : envEntries) {
                writer.startElement("o:envEntry");

                writer.addAttribute("name", entry.name);
                writer.addAttribute("type", entry.type);
                writer.addAttribute("value", entry.value);

                writer.endElement();
            }
        }

        // ejb refs
        List<EjbReferenceInfo> ejbRefs = AssemblerUtil.asList(bean.jndiEnc.ejbReferences);
        List<EjbLocalReferenceInfo> ejbLocalRefs = AssemblerUtil.asList(bean.jndiEnc.ejbLocalReferences);
        if (!ejbRefs.isEmpty() || !ejbLocalRefs.isEmpty()) {
            // remote ejb refs
            for (EjbReferenceInfo ejbRef : ejbRefs) {
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
            for (EjbLocalReferenceInfo ejbRef : ejbLocalRefs) {
                writer.startElement("o:ejbRef");

                writer.addAttribute("name", ejbRef.referenceName);
                writer.addAttribute("local", "true");
                writer.addAttribute("ejbId", ejbRef.location.ejbDeploymentId);

                writer.endElement();
            }
        }

        // resource refs
        List<ResourceReferenceInfo> resourceRefs = AssemblerUtil.asList(bean.jndiEnc.resourceRefs);
        if (!resourceRefs.isEmpty()) {
            for (ResourceReferenceInfo resourceRef : resourceRefs) {
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
        }

        writer.endElement();
        writer.endElement();
    }

    private void writeAssembly(OpenEjbConfiguration configInfo) {
        writer.startElement("o:assembly");
        writer.addAttribute("id", ASSEMBLY_ID);

        Map<String, String[]> map = new TreeMap<String, String[]>();
        for (RoleMappingInfo mapping : configInfo.facilities.securityService.roleMappings) {
            for (String logicalRoleName : mapping.logicalRoleNames) {
                if (map.containsKey(logicalRoleName)) {
                    throw new IllegalStateException("Logical role " + logicalRoleName + " is mapped to " +
                            Arrays.toString(map.get(logicalRoleName)) + " and " + mapping.physicalRoleNames);
                }
                map.put(logicalRoleName, mapping.physicalRoleNames);
            }
        }

        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String logicalRoleName = entry.getKey();
            String[] physicalRoleNames = entry.getValue();
            writer.startElement("o:roleMapping");
            writer.addAttribute("logical", logicalRoleName);
            writer.addAttribute("physical", createCsv(physicalRoleNames));
            writer.endElement();
        }

        for (MethodPermissionInfo permissionInfo : AssemblerUtil.asList(configInfo.containerSystem.methodPermissions)) {
            writer.startElement("o:permission");
            writer.addAttribute("roleNames", createCsv(permissionInfo.roleNames));
            writeMethodInfos(permissionInfo.methods);
            writer.endElement();
        }

        for (MethodTransactionInfo transactionInfo : AssemblerUtil.asList(configInfo.containerSystem.methodTransactions)) {
            writer.startElement("o:transaction");
            writer.addAttribute("transAttribute", transactionInfo.transAttribute);
            writeMethodInfos(transactionInfo.methods);
            writer.endElement();
        }

        writer.endElement();
    }

    private void writeMethodInfos(MethodInfo[] methods) {
        for (MethodInfo methodInfo : AssemblerUtil.asList(methods)) {
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
}
