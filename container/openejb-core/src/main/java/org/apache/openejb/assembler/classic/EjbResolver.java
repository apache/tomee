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

package org.apache.openejb.assembler.classic;

import org.apache.openejb.util.LinkResolver;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @version $Rev$ $Date$
 */
public class EjbResolver {

    public interface Reference {

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

    private final Map<String, EnterpriseBeanInfo> deployments = new TreeMap<>();
    private final LinkResolver<String> resolver = new LinkResolver<>();
    private final Map<Interfaces, List<Interfaces>> interfaces = new TreeMap<>();

    private final EjbResolver parent;

    private final Scope scope;

    public EjbResolver(final EjbResolver parent, final Scope scope, final EjbJarInfo... ejbJars) {
        this(parent, scope, Arrays.asList(ejbJars));
    }

    public EjbResolver(final EjbResolver parent, final Scope scope, final List<EjbJarInfo> ejbJars) {
        this.scope = scope;
        this.parent = parent;

        for (final EjbJarInfo ejbJarInfo : ejbJars) {
            add(ejbJarInfo);
        }
    }

    /**
     * Possible syncronization issue here
     */
    public void addAll(final List<EjbJarInfo> ejbJars) {
        for (final EjbJarInfo ejbJarInfo : ejbJars) {
            add(ejbJarInfo);
        }
    }

    public void add(final EjbJarInfo ejbJarInfo) {
        for (final EnterpriseBeanInfo bean : ejbJarInfo.enterpriseBeans) {
            index(ejbJarInfo.moduleUri, bean);
        }
    }

    private void index(final URI moduleURI, final EnterpriseBeanInfo bean) {
        // All deployments: deploymentId -> bean

        deployments.put(bean.ejbDeploymentId, bean);

        // add to the link resolver
        resolver.add(moduleURI, bean.ejbName, bean.ejbDeploymentId);

        // Remote: Interfaces(home,object) -> deploymentId
        if (bean.remote != null) {
            addInterfaces(new Interfaces(bean.home, bean.remote, Type.REMOTE, bean.ejbDeploymentId));
            addInterfaces(new Interfaces(bean.remote, Type.REMOTE, bean.ejbDeploymentId));
        }
        for (final String businessRemote : bean.businessRemote) {
            addInterfaces(new Interfaces(businessRemote, Type.REMOTE, bean.ejbDeploymentId));
        }

        // Local: Interfaces(home,object) -> deploymentId
        if (bean.local != null) {
            addInterfaces(new Interfaces(bean.localHome, bean.local, Type.LOCAL, bean.ejbDeploymentId));
            addInterfaces(new Interfaces(bean.local, Type.LOCAL, bean.ejbDeploymentId));
        }
        for (final String businessLocal : bean.businessLocal) {
            addInterfaces(new Interfaces(businessLocal, Type.LOCAL, bean.ejbDeploymentId));
        }

        if (bean.localbean) {
            addInterfaces(new Interfaces(bean.ejbClass, Type.LOCAL, bean.ejbDeploymentId));
            for (final String parent : bean.parents) {
                addInterfaces(new Interfaces(parent, Type.LOCAL, bean.ejbDeploymentId));
            }
        }
    }

    private void addInterfaces(final Interfaces interfaces) {
        List<Interfaces> similar = this.interfaces.computeIfAbsent(interfaces, k -> new ArrayList<>());
        similar.add(interfaces);
    }

    private String resolveLink(final String link, final URI moduleUri) {
        if (link == null || link.length() == 0) {
            return null;
        }

        String id = resolver.resolveLink(link, moduleUri);
        if (id == null && parent != null) {
            id = parent.resolveLink(link, moduleUri);
        }
        return id;
    }

    private String resolveInterface(final Reference ref) {
        String id = null;

        if (ref.getInterface() == null) {
            // TODO: Support home-only refs.  Could only happen for EJB 2.x Entity beans
            // All other beans are required to have create methods in their home interfaces
            // and we would have used it to discover the remote interface when creating the ref 
            return null;
        }

        final List<Interfaces> matches = this.interfaces.get(new Interfaces(ref.getHome(), ref.getInterface()));
        if (matches != null && matches.size() > 0) {

            final List<Interfaces> nameMatches = filter(matches, ref.getName());
            // Imply by name and type (local/remote)
            id = first(filter(nameMatches, ref.getRefType()));
            if (id == null) {
                // Match by name
                id = first(nameMatches);
            }
            // Imply by type (local/remote)
            if (id == null) {
                id = first(filter(matches, ref.getRefType()));
            }
            // Just grab the first
            if (id == null) {
                id = first(matches);
            }
        }

        if (id == null && parent != null) {
            id = parent.resolveInterface(ref);
        }

        return id;
    }

    private String first(final List<Interfaces> list) {
        if (list.size() == 0) {
            return null;
        }
        return list.get(0).getId();
    }

    private List<Interfaces> filter(final List<Interfaces> list, final String name) {
        final String shortName = name.replaceAll(".*/", "");
        final List<Interfaces> matches = new ArrayList();
        for (final Interfaces entry : list) {
            if (name.equalsIgnoreCase(entry.getId())) {
                matches.add(entry);
            } else if (shortName.equalsIgnoreCase(entry.getId())) {
                matches.add(entry);
            }
        }
        return matches;
    }

    private List<Interfaces> filter(final List<Interfaces> list, final Type type) {
        final List<Interfaces> matches = new ArrayList();
        for (final Interfaces entry : list) {
            if (type == Type.UNKNOWN || type == entry.type) {
                matches.add(entry);
            }
        }
        return matches;
    }

    public Scope getScope(final String deploymentId) {
        if (deployments.containsKey(deploymentId)) {
            return scope;
        }

        if (parent != null) {
            return parent.getScope(deploymentId);
        }

        return null;
    }

    public EnterpriseBeanInfo getEnterpriseBeanInfo(final String deploymentId) {
        EnterpriseBeanInfo info = deployments.get(deploymentId);
        if (info == null && parent != null) {
            info = parent.getEnterpriseBeanInfo(deploymentId);
        }
        return info;
    }

    public String resolve(final Reference ref, final URI moduleUri) {

        if (ref.getMappedName() != null && !ref.getMappedName().isEmpty()) {
            return ref.getMappedName();
        }

        String targetId = this.resolveLink(ref.getEjbLink(), moduleUri);

        if (targetId == null && ref.getEjbLink() == null) {
            targetId = resolveInterface(ref);
        }

        return targetId;
    }

    private static class Interfaces implements Comparable {
        private final String id;
        private final Type type;
        private final String homeInterface;
        private final String objectInterface;

        public Interfaces(final String objectInterface, final Type type, final String id) {
            if (objectInterface == null) {
                throw new NullPointerException("objectInterface is null");
            }
            this.homeInterface = "<none>";
            this.objectInterface = objectInterface;
            this.type = type;
            this.id = id;
        }

        public Interfaces(String homeInterface, final String objectInterface, final Type type, final String id) {
            if (homeInterface == null) {
                homeInterface = "<none>";
            }
            if (objectInterface == null) {
                throw new NullPointerException("objectInterface is null");
            }
            this.homeInterface = homeInterface;
            this.objectInterface = objectInterface;
            this.type = type;
            this.id = id;
        }

        public Interfaces(String homeInterface, final String objectInterface) {
            if (homeInterface == null) {
                homeInterface = "<none>";
            }
            if (objectInterface == null) {
                throw new NullPointerException("objectInterface is null");
            }
            this.homeInterface = homeInterface;
            this.objectInterface = objectInterface;
            this.type = null;
            this.id = null;
        }

        public String getId() {
            return id;
        }

        public Type getType() {
            return type;
        }

        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Interfaces that = (Interfaces) o;

            return homeInterface.equals(that.homeInterface) && objectInterface.equals(that.objectInterface);
        }

        public int hashCode() {
            int result;
            result = homeInterface.hashCode();
            result = 31 * result + objectInterface.hashCode();
            return result;
        }

        public int compareTo(final Object o) {
            if (this == o) {
                return 0;
            }

            final Interfaces that = (Interfaces) o;
            return toString().compareTo(that.toString());
        }

        public String toString() {
            return homeInterface + ":" + objectInterface;
        }
    }

}
