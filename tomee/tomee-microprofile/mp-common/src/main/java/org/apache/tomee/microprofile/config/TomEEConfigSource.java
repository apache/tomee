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
package org.apache.tomee.microprofile.config;

import io.smallrye.config.common.MapBackedConfigSource;

import java.util.Map;

/**
 * This class exists to disable the open telemetry sdk in the general case.
 */
public class TomEEConfigSource extends MapBackedConfigSource {
    public TomEEConfigSource() {
        super(TomEEConfigSource.class.getSimpleName(),
                Map.of("otel.sdk.disabled", "true",
                        "otel.traces.exporter", "none",
                        "otel.metrics.exporter", "none",
                        "otel.logs.exporter", "none"),
                Integer.MIN_VALUE);
    }
}
