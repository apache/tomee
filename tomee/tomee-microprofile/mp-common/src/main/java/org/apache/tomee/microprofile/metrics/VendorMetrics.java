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
package org.apache.tomee.microprofile.metrics;

import io.smallrye.metrics.SharedMetricRegistries;
import io.smallrye.metrics.legacyapi.LegacyMetricRegistryAdapter;
import org.apache.openejb.assembler.classic.event.AssemblerAfterApplicationCreated;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.util.Join;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;

import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class exposes various vendor-specific metrics
 * Our vendor specific stuff typically lives in openejb.management in JMX
 * We have a slight issue that it doesn't look like we
 * can tie it back to application (at the moment at least),
 * so we probably just need to scrape everything each time.
 */
public class VendorMetrics {

    private static final Logger LOGGER = Logger.getLogger(VendorMetrics.class.getName());

    public void afterApplicationDeployed(@Observes AssemblerAfterApplicationCreated event) {
        if ("none".equals(SystemInstance.get().getOptions().get("tomee.mp.scan", "none"))) {
            return;
        }

        final MetricRegistry registry = SharedMetricRegistries.getOrCreate(MetricRegistry.VENDOR_SCOPE);

        if (! (registry instanceof LegacyMetricRegistryAdapter)) {
            return;
        }

        final MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        final Set<ObjectInstance> objectInstances;

        try {
            objectInstances = platformMBeanServer.queryMBeans(new ObjectName("openejb.management:*"), null);
        } catch (MalformedObjectNameException e) {
            LOGGER.severe("Unable to read MBeans under openejb.management");
            return;
        }

        for (final ObjectInstance objectInstance : objectInstances) {
            final ObjectName objectName = objectInstance.getObjectName();
            LOGGER.info("Adding vendor metrics for " + objectName);
            LOGGER.info("Class name: " + objectInstance.getClassName());

            final List<String> nameParts = new ArrayList<>();

            if (objectName.getKeyProperty("ObjectType") != null) {
                nameParts.add(objectName.getKeyProperty("ObjectType"));
                if (objectName.getKeyProperty("DataSource") != null) {
                    nameParts.add(objectName.getKeyProperty("DataSource"));
                }
            } else if (objectName.getKeyProperty("j2eeType") != null) {
                nameParts.add(objectName.getKeyProperty("j2eeType"));
                if (objectName.getKeyProperty("name") != null) {
                    nameParts.add(objectName.getKeyProperty("name"));
                }
            }

            final String metricName = Join.join("_", nameParts);

            try {
                final MBeanInfo mBeanInfo = platformMBeanServer.getMBeanInfo(objectName);
                final JMXInfo jmxInfo = JMXInfo.from(objectName, mBeanInfo);

                jmxInfo.configureMetrics(registry, metricName);

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unable to configure metrics for " + objectName, e);
            }
        }
    }


    private static class JMXInfo {
        private final ObjectName objectName;
        private long lastUpdated = 0;
        private Map<String, JMXAttribute> attributeMap = new HashMap<>();

        private JMXInfo(ObjectName objectName) {
            this.objectName = objectName;
        }

        public static JMXInfo from(final ObjectName objectName, final MBeanInfo info) {

            final List<JMXAttribute> attributes = Arrays.stream(info.getAttributes())
                    .filter(a -> JMXAttribute.getType(a.getType()) != null)
                    .map(a -> JMXAttribute.from(a))
                    .collect(Collectors.toList());


            final JMXInfo jmxInfo = new JMXInfo(objectName);
            attributes.forEach(a -> {
                jmxInfo.attributeMap.put(a.getAttributeName(), a);
            });

            return jmxInfo;
        }

        public synchronized void update() {
            final long timeNow = System.currentTimeMillis();

            // try not to hammer JMX, maybe make this configurable?
            if ((timeNow - lastUpdated) < 5000) {
                return;
            }

            final MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            final String[] attributeNames = attributeMap.keySet().toArray(new String[0]);
            final AttributeList attributes;

            try {
                attributes = platformMBeanServer.getAttributes(objectName, attributeNames);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unable to read metrics from JMX", e);
                return;
            }

            attributes.asList().forEach(a -> {
                final String name = a.getName();
                final Object value = a.getValue();

                if (! Number.class.isInstance(value)) {
                    // Log?
                    return;
                }

                final JMXAttribute jmxAttribute = attributeMap.get(name);
                if (jmxAttribute == null) {
                    return;
                }

                jmxAttribute.setCurrentValue(Number.class.cast(value));
            });

            lastUpdated = timeNow;
        }

        public Double get(final String attributeName) {
            update();

            final JMXAttribute jmxAttribute = attributeMap.get(attributeName);
            if (jmxAttribute == null) {
                throw new IllegalArgumentException("Attribute name: " + attributeName + " not known");
            }

            return jmxAttribute.toDouble();
        }

        public void configureMetrics(final MetricRegistry registry, final String metricName) {
            attributeMap.values().forEach(a -> {
                ((LegacyMetricRegistryAdapter) registry).counter(
                    Metadata
                        .builder()
                        .withName(metricName + "_" + a.getAttributeName())
                        .build(),
                    a.getType(),
                    value -> get(a.attributeName)
                );
            });
        }
    }

    private static class JMXAttribute<T extends Number> {
        private final String attributeName;
        private final Class<T> type;
        private T currentValue;

        public JMXAttribute(String attributeName, Class<T> type) {
            this.attributeName = attributeName;
            this.type = type;
        }

        public static JMXAttribute from(final MBeanAttributeInfo attributeInfo) {
            final String name = attributeInfo.getName();
            final Class<? extends Number> t = JMXAttribute.getType(attributeInfo.getType());

            if (name == null || t == null) {
                throw new IllegalArgumentException("Class and type must be specified");
            }

            return new JMXAttribute(name, t);
        }

        public String getAttributeName() {
            return attributeName;
        }

        public Class<T> getType() {
            return type;
        }

        public T getCurrentValue() {
            return currentValue;
        }

        public void setCurrentValue(T currentValue) {
            this.currentValue = currentValue;
        }

        public Double toDouble() {
            if (currentValue == null) {
                return Double.NaN;
            }

            return currentValue.doubleValue();
        }

        @Override
        public String toString() {
            return attributeName + "(" + type.getName() + "): " + currentValue;
        }

        public static Class<? extends Number> getType(final String type) {
            if ("int".equals(type)) {
                return Integer.class;
            }
            if ("long".equals(type)) {
                return Long.class;
            }
            if ("float".equals(type)) {
                return Float.class;
            }
            if ("double".equals(type)) {
                return Double.class;
            }

            return null;
        }
    }
}
