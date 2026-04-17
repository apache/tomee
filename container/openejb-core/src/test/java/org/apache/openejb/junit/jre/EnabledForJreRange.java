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
package org.apache.openejb.junit.jre;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables a JUnit 4 test method only when the running JRE feature version falls in
 * {@code [min, max]}. Mirrors {@code org.junit.jupiter.api.condition.EnabledForJreRange}
 * for codebases that cannot depend on JUnit Jupiter. Tests outside the range are skipped
 * via {@code Assume} semantics (surefire reports them as skipped).
 *
 * <p>Must be combined with {@link JreConditionRule} as a {@code @Rule}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EnabledForJreRange {
    int min() default 0;
    int max() default Integer.MAX_VALUE;
}
