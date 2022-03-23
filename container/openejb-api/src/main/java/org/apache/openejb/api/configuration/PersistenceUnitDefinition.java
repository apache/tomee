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
package org.apache.openejb.api.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PersistenceUnitDefinition {
    String unitName() default "jpa";
    String provider() default "auto";
    String jtaDataSource() default "auto";
    String nonJtaDataSource() default "auto";
    String entitiesPackage() default "auto";
    String[] properties() default {};
    boolean ddlAuto() default true;
    boolean jta() default true;
    ValidationMode validationMode() default ValidationMode.AUTO;
    SharedCacheMode cacheMode() default SharedCacheMode.UNSPECIFIED;
}
