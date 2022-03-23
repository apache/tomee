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

package org.apache.openejb.spi;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation intented to represent the principal that should be returned
 * from calls to jakarta.ejb.EJBContext.getCallerPrincipal()
 *
 * Implementations of org.apache.openejb.spi.SecurityService are encouraged
 * to return a java.security.Principal object that implements CallerPrinciple
 *
 * @version $Rev$ $Date$
 */
@Target(value = {TYPE})
@Retention(value = RUNTIME)
public @interface CallerPrincipal {
}
