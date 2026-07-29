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
package org.superbiz.rest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.annotation.JsonbVisibility;
import jakarta.json.bind.config.PropertyVisibilityStrategy;
import jakarta.json.bind.spi.JsonbProvider;
import static org.junit.Assert.assertEquals;

/**
 * Test JsonbVisibility for JsonB handling of records
 */
public class JsonbRecordMethodVisibilityTest {

    @Test
    public void testToJson() {
        Jsonb jsonb = JsonbProvider.provider().create().build();

        final String json = jsonb.toJson(new Person(45, "Clara"));
        assertEquals("{\"age\":45,\"name\":\"Clara\"}", json);
    }

    @JsonbVisibility(RecordVisibilityStrategy.class)
    public static record Person(int age, String name) {

        public String personInfo() {
            return name + "/" + age;
        }
    }

    public static class RecordVisibilityStrategy implements PropertyVisibilityStrategy {
        @Override
        public boolean isVisible(Field field) {
            return true;
        }

        @Override
        public boolean isVisible(Method method) {
            return !"personInfo".equals(method.getName());
        }
    }
}
