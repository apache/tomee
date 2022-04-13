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
package org.apache.tomee.microprofile.health;

import io.smallrye.health.SmallRyeHealth;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class MicroProfileHealthReporter {

    public static final String DOWN = "DOWN";
    public static final String UP = "UP";
    private final boolean defaultServerProceduresDisabled;
    private final String defaultReadinessEmptyResponse;
    private final String defaultStartupEmptyResponse;
    private Map<HealthCheck, ClassLoader> healthChecks = new HashMap<>();
    private Map<HealthCheck, ClassLoader> livenessChecks = new HashMap<>();
    private Map<HealthCheck, ClassLoader> readinessChecks = new HashMap<>();
    private Map<HealthCheck, ClassLoader> startupChecks = new HashMap<>();
    private Map<HealthCheck, ClassLoader> serverReadinessChecks = new HashMap<>();

    private final HealthCheck emptyDeploymentLivenessCheck;
    private final HealthCheck emptyDeploymentReadinessCheck;
    private final HealthCheck emptyDeploymentStartupCheck;

    private boolean userChecksProcessed = false;

    private static class EmptyDeploymentCheckStatus implements HealthCheck {
        private final String name;
        private final String status;

        EmptyDeploymentCheckStatus(String name, String status) {
            this.name = name;
            this.status = status;
        }

        @Override
        public HealthCheckResponse call() {
            return HealthCheckResponse.named(name)
                    .status(status.equals("UP"))
                    .build();
        }
    }

    public MicroProfileHealthReporter() {
        this.emptyDeploymentLivenessCheck  = new EmptyDeploymentCheckStatus("empty-liveness-checks", UP);
        this.emptyDeploymentReadinessCheck  = new EmptyDeploymentCheckStatus("empty-readiness-checks", UP);
        this.emptyDeploymentStartupCheck  = new EmptyDeploymentCheckStatus("empty-startup-checks", UP);
        this.defaultServerProceduresDisabled = false;
        this.defaultReadinessEmptyResponse = DOWN;
        this.defaultStartupEmptyResponse = DOWN;
    }

    public MicroProfileHealthReporter(String emptyLivenessChecksStatus, String emptyReadinessChecksStatus,
                                      String emptyStartupChecksStatus, boolean defaultServerProceduresDisabled,
                                      String defaultReadinessEmptyResponse, String defaultStartupEmptyResponse) {

        this.emptyDeploymentLivenessCheck  = new EmptyDeploymentCheckStatus("empty-liveness-checks", emptyLivenessChecksStatus);
        this.emptyDeploymentReadinessCheck  = new EmptyDeploymentCheckStatus("empty-readiness-checks", emptyReadinessChecksStatus);
        this.emptyDeploymentStartupCheck  = new EmptyDeploymentCheckStatus("empty-startup-checks", emptyStartupChecksStatus);
        this.defaultServerProceduresDisabled = defaultServerProceduresDisabled;
        this.defaultReadinessEmptyResponse = defaultReadinessEmptyResponse;
        this.defaultStartupEmptyResponse = defaultStartupEmptyResponse;
    }

    public SmallRyeHealth getHealth() {
        HashMap<HealthCheck, ClassLoader> deploymentChecks = new HashMap<>();
        deploymentChecks.putAll(healthChecks);
        deploymentChecks.putAll(livenessChecks);
        deploymentChecks.putAll(readinessChecks);
        deploymentChecks.putAll(startupChecks);

        HashMap<HealthCheck, ClassLoader> serverChecks= new HashMap<>();
        serverChecks.putAll(serverReadinessChecks);
        if (deploymentChecks.size() == 0 && !defaultServerProceduresDisabled) {
            serverChecks.put(emptyDeploymentLivenessCheck, Thread.currentThread().getContextClassLoader());
            serverChecks.put(emptyDeploymentReadinessCheck, Thread.currentThread().getContextClassLoader());
            serverChecks.put(emptyDeploymentStartupCheck, Thread.currentThread().getContextClassLoader());
        }

        return getHealth(serverChecks, deploymentChecks);
    }

    public SmallRyeHealth getLiveness() {
        final Map<HealthCheck, ClassLoader> serverChecks;
        if (livenessChecks.size() == 0 && !defaultServerProceduresDisabled) {
            serverChecks = Collections.singletonMap(emptyDeploymentLivenessCheck, Thread.currentThread().getContextClassLoader());
        } else {
            serverChecks = Collections.emptyMap();
        }
        return getHealth(serverChecks, livenessChecks);
    }

    public SmallRyeHealth getReadiness() {
        final Map<HealthCheck, ClassLoader> serverChecks = new HashMap<>();
        serverChecks.putAll(serverReadinessChecks);
        if (readinessChecks.size() == 0) {
            if (defaultServerProceduresDisabled) {
                return getHealth(serverChecks, readinessChecks,
                    userChecksProcessed ? HealthCheckResponse.Status.UP :
                        HealthCheckResponse.Status.valueOf(defaultReadinessEmptyResponse));
            } else {
                serverChecks.put(emptyDeploymentReadinessCheck, Thread.currentThread().getContextClassLoader());
                return getHealth(serverChecks, readinessChecks);
            }
        }
        return getHealth(serverChecks, readinessChecks);
    }

    public SmallRyeHealth getStartup() {
        Map<HealthCheck, ClassLoader> serverChecks = Collections.emptyMap();
        if (startupChecks.size() == 0) {
            if (defaultServerProceduresDisabled) {
                return getHealth(serverChecks, startupChecks,
                    userChecksProcessed ? HealthCheckResponse.Status.UP :
                        HealthCheckResponse.Status.valueOf(defaultStartupEmptyResponse));
            } else {
                serverChecks = Collections.singletonMap(emptyDeploymentStartupCheck, Thread.currentThread().getContextClassLoader());
                return getHealth(serverChecks, startupChecks);
            }
        }
        return getHealth(serverChecks, startupChecks);
    }

    private SmallRyeHealth getHealth(Map<HealthCheck, ClassLoader> serverChecks, Map<HealthCheck, ClassLoader> deploymentChecks) {
        return getHealth(serverChecks, deploymentChecks, HealthCheckResponse.Status.UP);
    }

    private SmallRyeHealth getHealth(Map<HealthCheck, ClassLoader> serverChecks, Map<HealthCheck,
        ClassLoader> deploymentChecks, HealthCheckResponse.Status defaultStatus) {
        JsonArrayBuilder results = Json.createArrayBuilder();
        HealthCheckResponse.Status status = defaultStatus;

        status = processChecks(serverChecks, results, status);

        status = processChecks(deploymentChecks, results, status);

        JsonObjectBuilder builder = Json.createObjectBuilder();

        JsonArray checkResults = results.build();

        builder.add("status", status.toString());
        builder.add("checks", checkResults);

        JsonObject build = builder.build();

        if (status.equals(HealthCheckResponse.Status.DOWN)) {
            Logger.getInstance(LogCategory.OPENEJB, MicroProfileHealthReporter.class).error("Reporting DOWN status: " + build.toString());
        }

        return new SmallRyeHealth(build);
    }

    private HealthCheckResponse.Status processChecks(Map<HealthCheck, ClassLoader> checks, JsonArrayBuilder results, HealthCheckResponse.Status status) {
        if (checks != null) {
            for (Map.Entry<HealthCheck, ClassLoader> entry : checks.entrySet()) {
                // use the classloader of the deployment's module instead of the TCCL (which is the server's ModuleClassLoader
                // to ensure that any resources that checks the TCCL (such as MP Config) will use the correct one
                // when the health checks are called.
                final ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(entry.getValue());
                    status = fillCheck(entry.getKey(), results, status);
                } finally {
                    Thread.currentThread().setContextClassLoader(oldTCCL);
                }
            }
        }

        return status;
    }

    private HealthCheckResponse.Status fillCheck(HealthCheck check, JsonArrayBuilder results, HealthCheckResponse.Status globalOutcome) {
        JsonObject each = jsonObject(check);
        results.add(each);
        if (globalOutcome == HealthCheckResponse.Status.UP) {
            String status = each.getString("status");
            if (status.equals(DOWN)) {
                return HealthCheckResponse.Status.DOWN;
            }
        }
        return globalOutcome;
    }

    private JsonObject jsonObject(HealthCheck check) {
        try {
            return jsonObject(check.call());
        } catch (RuntimeException e) {
            // Log Stacktrace to server log so an error is not just in Health Check response
            Logger.getInstance(LogCategory.OPENEJB, MicroProfileHealthReporter.class).error("Error processing Health Checks", e);

            HealthCheckResponseBuilder response = HealthCheckResponse.named(check.getClass().getName()).down();

            return jsonObject(response.build());
        }
    }

    private JsonObject jsonObject(HealthCheckResponse response) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("name", response.getName());
        builder.add("status", response.getStatus().toString());
        response.getData().ifPresent(d -> {
            JsonObjectBuilder data = Json.createObjectBuilder();
            for (Map.Entry<String, Object> entry : d.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    data.add(entry.getKey(), (String) value);
                } else if (value instanceof Long) {
                    data.add(entry.getKey(), (Long) value);
                } else if (value instanceof Boolean) {
                    data.add(entry.getKey(), (Boolean) value);
                }
            }
            builder.add("data", data.build());
        });

        return builder.build();
    }

    public void addHealthCheck(HealthCheck check, ClassLoader moduleClassLoader) {
        if (check != null) {
            healthChecks.put(check, moduleClassLoader);
        }
    }

    public void removeHealthCheck(HealthCheck check) {
        healthChecks.remove(check);
    }

    public void addReadinessCheck(HealthCheck check, ClassLoader moduleClassLoader) {
        if (check != null) {
            readinessChecks.put(check, moduleClassLoader);
        }
    }

    public void addServerReadinessCheck(HealthCheck check, ClassLoader moduleClassLoader) {
        if (check != null) {
            serverReadinessChecks.put(check, moduleClassLoader);
        }
    }

    public void removeReadinessCheck(HealthCheck check) {
        readinessChecks.remove(check);
    }

    public void addLivenessCheck(HealthCheck check, ClassLoader moduleClassLoader) {
        if (check != null) {
            livenessChecks.put(check, moduleClassLoader);
        }
    }

    public void removeLivenessCheck(HealthCheck check) {
        livenessChecks.remove(check);
    }

    public void addStartupCheck(HealthCheck check, ClassLoader moduleClassLoader) {
        if (check != null) {
            startupChecks.put(check, moduleClassLoader);
        }
    }

    public void removeStartupCheck(HealthCheck check) {
        startupChecks.remove(check);
    }

    public void setUserChecksProcessed(boolean userChecksProcessed) {
        this.userChecksProcessed = userChecksProcessed;
    }
}