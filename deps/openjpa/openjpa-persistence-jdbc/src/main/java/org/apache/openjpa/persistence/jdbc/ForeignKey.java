/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.jdbc;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Foreign key definition.
 *
 * @author Abe White
 * @since 0.4.0
 * @published
 */
@Target({ TYPE, METHOD, FIELD })
@Retention(RUNTIME)
public @interface ForeignKey {

    String name() default "";

    boolean enabled() default true;

    boolean deferred() default false;

    ForeignKeyAction deleteAction() default ForeignKeyAction.RESTRICT;

    ForeignKeyAction updateAction() default ForeignKeyAction.RESTRICT;

    String[] columnNames() default {};

    boolean specified() default true;
    
    boolean implicit() default false;
}
