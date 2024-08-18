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
package org.apache.openejb.threads.impl;

import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class ContextServiceImplFactory {
    public static final String AUTOMATIC_SINGLETON = "[automatic]";

    private static ContextServiceImpl defaultSingleton;

    private final List<String> propagated = new ArrayList<>();
    private final List<String> cleared = new ArrayList<>();
    private final List<String> unchanged = new ArrayList<>();

    public String getPropagated() {
        return Join.join(",", propagated);
    }

    public void setPropagated(final String propagated) {
        this.propagated.clear();
        if (propagated != null && propagated.length() > 0) {
            this.propagated.addAll(Arrays.asList(propagated.split(" *, *")));
        }
    }

    public String getCleared() {
        return Join.join(",", cleared);
    }

    public void setCleared(final String cleared) {
        this.cleared.clear();
        if (cleared != null && cleared.length() > 0) {
            this.cleared.addAll(Arrays.asList(cleared.split(" *, *")));
        }
    }

    public String getUnchanged() {
        return Join.join(",", unchanged);
    }

    public void setUnchanged(final String unchanged) {
        this.unchanged.clear();
        if (unchanged != null && unchanged.length() > 0) {
            this.unchanged.addAll(Arrays.asList(unchanged.split(" *, *")));
        }
    }

    public static ContextServiceImpl newDefaultContextService() {
        ContextServiceImplFactory factory = new ContextServiceImplFactory();

        // It's unclear what the default is, spec says (§ 3.3.4.3 Default Context Service):
        //   The types of contexts to be propagated by this default ContextService from a contextualizing application component
        //   must include naming context, class loader, and security information.
        // But @ContextServiceDefinition defaults to propagated="Remaining", cleared="Transaction" unchanged=""
        factory.setPropagated(ContextServiceDefinition.ALL_REMAINING);
        factory.setCleared(ContextServiceDefinition.TRANSACTION);

        return factory.create();
    }

    public static ContextServiceImpl newPropagateEverythingContextService() {
        final ContextServiceImplFactory factory = new ContextServiceImplFactory();
        factory.setPropagated(ContextServiceDefinition.ALL_REMAINING);
        return factory.create();
    }


    public static ContextServiceImpl getOrCreateDefaultSingleton() {
        // Synchronization is left out here,
        // it's rather expensive and there is no issue with multiple ContextServiceImpl being created in rare cases
        // as there's no need to close/dispose it in a special manner
        if (defaultSingleton == null) {
            defaultSingleton = newDefaultContextService();
        }

        return defaultSingleton;
    }

    /**
     * Looks up a ContextServiceImpl using the specified name.
     * <code>[implicit]</code> is a special name that skips any JNDI lookups
     * and immediately returns {@link ContextServiceImplFactory#getOrCreateDefaultSingleton()}.
     * If the lookup fails (no name given or ContextService not bound) a warning is logged
     * to inform the user about a misconfiguration,
     * {@link ContextServiceImplFactory#getOrCreateDefaultSingleton()} is returned as a graceful fallback.
     */
    public static ContextServiceImpl lookupOrDefault(String name) {
        if (AUTOMATIC_SINGLETON.equals(name)) {
            return getOrCreateDefaultSingleton();
        }

        if (name == null || name.trim().isEmpty()) {
            Logger.getInstance(LogCategory.OPENEJB, ContextServiceImplFactory.class)
                    .warning("ContextService name is unspecified, falling back to default ContextService");

            return ContextServiceImplFactory.getOrCreateDefaultSingleton();
        }

        try {
            final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            final Context context = containerSystem.getJNDIContext();
            final Object obj = context.lookup("openejb/Resource/" + name);
            if (!(obj instanceof ContextServiceImpl)) {
                throw new IllegalArgumentException("Resource with id " + context
                        + " is not a ContextService, but is " + obj.getClass().getName());
            }
            return (ContextServiceImpl) obj;
        } catch (final NamingException e) {
            Logger.getInstance(LogCategory.OPENEJB, ContextServiceImplFactory.class)
                    .warning("Can't look up ContextService \"" + name + "\", falling back to default ContextService");

            return ContextServiceImplFactory.getOrCreateDefaultSingleton();
        }
    }

    public ContextServiceImpl create() {
        // some complication around this is ContextServiceDefinition.ALL_REMAINING, which looks like it
        // should reference everything that isn't explicitly mentioned in this.propagated, this.cleared and this.unchanged.

        // These are the defaults:

        // String[] propagated() default { ALL_REMAINING };
        // String[] cleared() default { TRANSACTION };
        // String[] unchanged() default {};

        // These are specified on the annotation, but we could also apply TRANSACTION to cleared, and ALL_REMAINING to propagated
        // if they are not explicitly set anywhere

        // not sure if there is some sort of priority if a context is listed in more than one bucket...,
        // let's assume propagated, cleared, unchanged is the order

        // Let's build a list of stuff


        final Map<String, ThreadContextProvider> threadContextProviders = new HashMap<>();

        // add the in-build ThreadContextProviders
        threadContextProviders.put(ContextServiceDefinition.APPLICATION, new ApplicationThreadContextProvider());
        threadContextProviders.put(ContextServiceDefinition.SECURITY, new SecurityThreadContextProvider());
        threadContextProviders.put(ContextServiceDefinition.TRANSACTION, new TxThreadContextProvider());
        getThreadContextProviders().forEach(t -> threadContextProviders.putIfAbsent(t.getThreadContextType(), t));

        // Let's resolve what should actually be in each bucket:
        // * specified contexts that don't actually exist should be ignored
        // * ALL_REMAINING should be changed into a list of contexts that actually are remaining

        final List<ThreadContextProvider> resolvedPropagated = new ArrayList<>();
        final List<ThreadContextProvider> resolvedCleared = new ArrayList<>();
        final List<ThreadContextProvider> resolvedUnchanged = new ArrayList<>();

        resolve(resolvedPropagated, propagated, threadContextProviders);
        resolve(resolvedCleared, cleared, threadContextProviders);
        resolve(resolvedUnchanged, unchanged, threadContextProviders);

        if (propagated.contains(ContextServiceDefinition.ALL_REMAINING)) {
            resolvedPropagated.addAll(threadContextProviders.values());
            threadContextProviders.clear();
        }

        if (cleared.contains(ContextServiceDefinition.ALL_REMAINING)) {
            resolvedCleared.addAll(threadContextProviders.values());
            threadContextProviders.clear();
        }

        if (unchanged.contains(ContextServiceDefinition.ALL_REMAINING)) {
            resolvedUnchanged.addAll(threadContextProviders.values());
            threadContextProviders.clear();
        }

        // check if anything is left: TRANSACTION should go on cleared, everything else
        // should go on propagated

        if (threadContextProviders.containsKey(ContextServiceDefinition.TRANSACTION)) {
            resolvedCleared.add(threadContextProviders.remove(ContextServiceDefinition.TRANSACTION));
        }

        resolvedPropagated.addAll(threadContextProviders.values());
        threadContextProviders.clear();

        // TODO: we could log the awesome work we have done to figure all this out
        // TODO: additionally, this should all be incredibly easy to unit test

//        boolean suspendTx = resolvedCleared.contains(ContextServiceDefinition.TRANSACTION);
//
//        // this takes precedence over where ContextServiceDefinition.TRANSACTION is set
//        if (ManagedTask.SUSPEND.equals(properties.get(ManagedTask.TRANSACTION))) {
//            suspendTx = true;
//        }

        return new ContextServiceImpl(resolvedPropagated, resolvedCleared, resolvedUnchanged);
    }

    protected List<ThreadContextProvider> getThreadContextProviders() {
        final List<ThreadContextProvider> result = new ArrayList<>();
        for (ThreadContextProvider tcp : ServiceLoader.load(ThreadContextProvider.class)) {
            result.add(tcp);
        }

        return result;
    }

    private void resolve(final List<ThreadContextProvider> providers, final List<String> specified, final Map<String, ThreadContextProvider> availableProviders) {
        for (String specifiedProviderName : specified) {
            if (availableProviders.containsKey(specifiedProviderName)) {
                providers.add(availableProviders.remove(specifiedProviderName));
            }
        }
    }
}
