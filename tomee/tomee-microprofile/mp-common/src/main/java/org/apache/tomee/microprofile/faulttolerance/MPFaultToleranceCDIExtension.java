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
package org.apache.tomee.microprofile.faulttolerance;

import io.smallrye.faulttolerance.FaultToleranceBinding;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import org.apache.openejb.loader.SystemInstance;
import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class is more a hack than an actual peace of integration code for SmallRye Fault Tolerance. It addresses an issue
 * in SmallRye because it's relying on a Weld behavior as opposed to a specified CDI feature. Or the spec is not clear
 * enough such as OWB and Weld did a different implementation. There is a good opportunity to add some TCKs to Jakarta
 */
public class MPFaultToleranceCDIExtension implements Extension {

    private List<Class> faultToleranceAnnotations = Arrays.asList(
        CircuitBreaker.class,
        Retry.class,
        Timeout.class,
        Asynchronous.class,
        Fallback.class, // this one can only be on methods
        Bulkhead.class);

    /**
     * Observer to a CDI lifecycle event to correctly add the interceptor binding to the actual bean. The SmallRye
     * extension adds the interceptor binding to the interceptor binding.
     *
     * This will go through classes annotations and add the FaultToleranceBinding on the type if it has one of the known
     * fault tolerance annotations. In case, fault tolerance annotations are applied on some methods of a class, we
     * also look for annotations on methods.
     *
     * As soon as we find at least one annotation, either on the class or one of the method, we add the interceptor
     * binding.
     *
     * @param pat CDI lifecycle callback payload
     * @param <X> Type of the Injection to observe
     */
    <X> void addFaultToleranceInterceptorBinding(@Observes final ProcessAnnotatedType<X> pat, final BeanManager bm) {

        // check fault tolerance annotations on classes
        if (hasFaultToleranceAnnotations(pat.getAnnotatedType())) {
            pat.configureAnnotatedType().add(FaultToleranceBinding.Literal.INSTANCE);
            return;
        }

        // if not on the class, it may be per method
        for (AnnotatedMethod<? super X> m : pat.getAnnotatedType().getMethods()) {
            if (hasFaultToleranceAnnotations(m)) {
                pat.configureAnnotatedType().add(FaultToleranceBinding.Literal.INSTANCE);
                return;
            }
        }
    }

    private boolean hasFaultToleranceAnnotations(final Annotated annotated) {
        for (Class annotation : faultToleranceAnnotations) {
            if (annotated.isAnnotationPresent(annotation)) {
                return true;
            }
        }
        return false;
    }
}