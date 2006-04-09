/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.openejb.ejbjar;

import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class Javaee {

    public abstract static class AbstractEjbRef extends JndiEnvironmentRef {
        String ejbRefName;
        EjbRefType ejbRefType;
        String localHome;
        String local;
        String ejbLink;
    }

    public static class EjbLocalRef extends AbstractEjbRef {
        String localHome;
        String local;
    }

    public static class EjbRef extends AbstractEjbRef {
        String home;
        String remote;
    }

    public static class EnvEntry extends JndiEnvironmentRef {
        String envEntryName;
        String envEntryType;
        String envEntryValue;

    }
    public static class Icon {
        String id;
        String smallIcon;
        String largeIcon;

    }
    public static class InjectionTarget {
        String injectionTargetClass;
        String injectionTargetName;
    }

    public static enum EjbRefType {
        ENTITY,
        SESSION;
    }

    public static class Listener {
        String id;
        List<String> description;
        List<String> displayName;
        List<Icon> icons;
        String listenerClass;
    }

    public static class JndiEnvironmentRef {
        String id;
        List<String> description;
        String mappedName;
        List<InjectionTarget> injectionTargets;
    }

    public static class MessageDestinationRef extends JndiEnvironmentRef {
        String messageDestinationRefName;
        String messageDestinationType;
        MessageDestinationUsage messageDestinationUsage;
        String messageDestinationLink;
    }

    public static enum MessageDestinationUsage {
        CONSUMES,
        PRODUCES,
        CONSUMESPRODUCES;
    }

    public static class MessageDestination {
        String id;
        String messageDestinationName;
        List<String> description;
        List<String> displayName;
        List<Icon> icons;
        String mappedName;
    }

    // TODO: Not used yet
    public static class ParamValue {
        String id;
        List<String> description;
        String paramName;
        String paramType;
    }


    public static class PersistenceContextRef extends JndiEnvironmentRef {
        String persistenceContextRefName;
        String persistenceUnitName;
        PersistenceContextType persistenceContextType;
        List<PersistenceProperty> persistenceProperties;

    }

    public static class Property {
        String id;
        String name;
        String value;
    }

    public static class PersistenceProperty extends Property {
    }

    public static enum PersistenceContextType {
        TRANSACTION,
        EXTENDED;
    }

    public static class PersistenceUnitRef extends JndiEnvironmentRef {
        String persistenceUnitRefName;
        String persistenceUnitName;
    }

    public static enum ResAuth {
        APPLICATION,
        CONTAINER;
    }

    public static enum ResourceSharingScope {
        SHARABLE,
        UNSHARABLE;
    }

    public static class ResourceEnvRef extends JndiEnvironmentRef {
        String resourceEnvRefName;
        String resourceEnvRefType;
    }

    public static class ResourceRef extends JndiEnvironmentRef {
        String resRefName;
        String resType;
        ResAuth resAuth;
        ResourceSharingScope resourceSharingScope;
    }

    public static class RunAs {
        String id;
        List<String> description;
        String roleName;
    }

    public static class PostConstruct extends LifecycleCallback {
    }

    public static class PreDestroy extends LifecycleCallback {
    }

    public static class LifecycleCallback {
        String lifecycleCallbackClass;
        String lifecycleCallbackMethod;
    }

    public static class SecurityRole {
        String id;
        List<String> description;
        String roleName;
    }

    public static class SecurityRoleRef {
        String id;
        List<String> description;
        String roleName;
        String roleLink;
    }
}
