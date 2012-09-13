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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.embedded;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.arquillian.common.IO;
import org.apache.openejb.arquillian.common.Prefixes;
import org.apache.openejb.arquillian.common.TomEEConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
@Prefixes({"tomee", "tomee.embedded"})
public class EmbeddedTomEEConfiguration extends TomEEConfiguration {
    @Override
    public int[] portsAlreadySet() {
        final List<Integer> value = new ArrayList<Integer>();
        if (getStopPort() > 0) {
            value.add(getStopPort());
        }
        if (getHttpPort() > 0) {
            value.add(getHttpPort());
        }
        return toInts(value);
    }

    public Properties systemPropertiesAsProperties() {
        if (properties == null || properties.isEmpty()) {
            return new Properties();
        }

        final Properties properties = new Properties();
        final ByteArrayInputStream bais = new ByteArrayInputStream(getProperties().getBytes());
        try {
            properties.load(bais);
        } catch (IOException e) {
            throw new OpenEJBRuntimeException(e);
        } finally {
            try {
                IO.close(bais);
            } catch (IOException ignored) {
                // no-op
            }
        }
        return properties;
    }
}
