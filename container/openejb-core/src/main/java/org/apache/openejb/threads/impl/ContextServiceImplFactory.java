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
import org.apache.openejb.util.Join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class ContextServiceImplFactory {

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

        final ContextServiceImpl contextService = new ContextServiceImpl();
        contextService.getPropagated().addAll(resolvedPropagated);
        contextService.getCleared().addAll(resolvedCleared);
        contextService.getUnchanged().addAll(resolvedUnchanged);

        return contextService;
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
