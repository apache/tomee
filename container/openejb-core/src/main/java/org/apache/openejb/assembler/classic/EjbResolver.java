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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.util.LinkResolver;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Arrays;

/**
 * @version $Rev$ $Date$
 */
public class EjbResolver {

    public static interface Reference {

        String getName();

        Type getRefType();

        String getHome();

        String getInterface();

        String getMappedName();

        String getEjbLink();
    }

    public static enum Type {
        UNKNOWN, LOCAL, REMOTE;
    }

    public static enum Scope {
        GLOBAL, EAR, EJBJAR;
    }

    private final Map<String, EnterpriseBeanInfo> deployments = new TreeMap<String, EnterpriseBeanInfo>();
    private final LinkResolver<String> resolver = new LinkResolver<String>();
    private final Map<Interfaces, String> remoteInterfaces = new TreeMap<Interfaces, String>();
    private final Map<Interfaces, String> localInterfaces = new TreeMap<Interfaces, String>();

    private EjbResolver parent;

    private final Scope scope;

    public EjbResolver(EjbResolver parent, Scope scope, EjbJarInfo... ejbJars) {
        this(parent, scope, Arrays.asList(ejbJars));
    }

    public EjbResolver(EjbResolver parent, Scope scope, List<EjbJarInfo> ejbJars) {
        this.scope = scope;
        this.parent = parent;

        for (EjbJarInfo ejbJarInfo : ejbJars) {
            add(ejbJarInfo);
        }
    }

    /**
     * Possible syncronization issue here
     */
    public void addAll(List<EjbJarInfo> ejbJars) {
        for (EjbJarInfo ejbJarInfo : ejbJars) {
            add(ejbJarInfo);
        }
    }

    private void add(EjbJarInfo ejbJarInfo) {
        for (EnterpriseBeanInfo bean : ejbJarInfo.enterpriseBeans) {
            index(ejbJarInfo.moduleId, bean);
        }
    }

    private void index(String moduleId, EnterpriseBeanInfo bean) {
        // All deployments: deploymentId -> bean
        deployments.put(bean.ejbDeploymentId, bean);

        // add to the link resolver
        resolver.add(moduleId, bean.ejbName, bean.ejbDeploymentId);

        // Remote: Interfaces(home,object) -> deploymentId
        if (bean.remote != null) {
            remoteInterfaces.put(new Interfaces(bean.home, bean.remote), bean.ejbDeploymentId);
            remoteInterfaces.put(new Interfaces(bean.remote), bean.ejbDeploymentId);
        }
        for (String businessRemote : bean.businessRemote) {
            remoteInterfaces.put(new Interfaces(businessRemote), bean.ejbDeploymentId);
        }

        // Local: Interfaces(home,object) -> deploymentId
        if (bean.local != null) {
            localInterfaces.put(new Interfaces(bean.localHome, bean.local), bean.ejbDeploymentId);
            localInterfaces.put(new Interfaces(bean.local), bean.ejbDeploymentId);
        }
        for (String businessLocal : bean.businessLocal) {
            localInterfaces.put(new Interfaces(businessLocal), bean.ejbDeploymentId);
        }
    }

    private String resolveLink(String link, URI moduleUri) {
        if (link == null || link.length() == 0) return null;

        String id = resolver.resolveLink(link, moduleUri);
        if (id == null && parent != null) {
            id = parent.resolveLink(link, moduleUri);
        }
        return id;
    }

    private String resolveInterface(Type type, String homeInterface, String objectInterface) {
        Interfaces interfaces = new Interfaces(homeInterface, objectInterface);
        return resolveInterface(type, interfaces);
    }

    private String resolveInterface(Type type, Interfaces interfaces) {
        String id;
        switch (type) {
            case UNKNOWN:
            case REMOTE: {
                id = remoteInterfaces.get(interfaces);
                id = (id != null) ? id : localInterfaces.get(interfaces);
            }
            break;
            case LOCAL: {
                id = localInterfaces.get(interfaces);
                id = (id != null) ? id : remoteInterfaces.get(interfaces);
            }
            break;
            default:
                id = null;
        }

        if (id == null && parent != null) {
            id = parent.resolveInterface(type, interfaces);
        }

        return id;
    }

    public Scope getScope(String deploymentId) {
        if (deployments.containsKey(deploymentId)) return scope;

        if (parent != null) return parent.getScope(deploymentId);

        return null;
    }

    public EnterpriseBeanInfo getEnterpriseBeanInfo(String deploymentId) {
        EnterpriseBeanInfo info = deployments.get(deploymentId);
        if (info == null && parent != null) {
            info = parent.getEnterpriseBeanInfo(deploymentId);
        }
        return info;
    }

    public String resolve(Reference ref, URI moduleUri) {

        if (ref.getMappedName() != null && !ref.getMappedName().equals("")) {
            return ref.getMappedName();
        }

        String targetId = this.resolveLink(ref.getEjbLink(), moduleUri);

        if (targetId == null && ref.getEjbLink() == null) {
            targetId = this.resolveInterface(ref.getRefType(), ref.getHome(), ref.getInterface());
        }

        return targetId;
    }

    private static class Interfaces implements Comparable {
        private final String homeInterface;
        private final String objectInterface;

        public Interfaces(String objectInterface) {
            if (objectInterface == null) throw new NullPointerException("objectInterface is null");
            this.homeInterface = "<none>";
            this.objectInterface = objectInterface;
        }

        public Interfaces(String homeInterface, String objectInterface) {
            if (homeInterface == null) homeInterface = "<none>";
            if (objectInterface == null) throw new NullPointerException("objectInterface is null");
            this.homeInterface = homeInterface;
            this.objectInterface = objectInterface;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Interfaces that = (Interfaces) o;

            return homeInterface.equals(that.homeInterface) && objectInterface.equals(that.objectInterface);
        }

        public int hashCode() {
            int result;
            result = homeInterface.hashCode();
            result = 31 * result + objectInterface.hashCode();
            return result;
        }

        public int compareTo(Object o) {
            if (this == o) return 0;

            Interfaces that = (Interfaces) o;
            return toString().compareTo(that.toString());
        }

        public String toString() {
            return homeInterface + ":" + objectInterface;
        }
    }

}
