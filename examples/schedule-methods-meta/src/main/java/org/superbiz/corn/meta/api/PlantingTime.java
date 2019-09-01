/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.corn.meta.api;

import javax.ejb.Schedule;
import javax.ejb.Schedules;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Metatype
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface PlantingTime {

    public static interface $ {

        @PlantingTime
        @Schedules({
                @Schedule(month = "5", dayOfMonth = "20-Last", minute = "0", hour = "8"),
                @Schedule(month = "6", dayOfMonth = "1-10", minute = "0", hour = "8")
        })
        public void method();
    }
}
