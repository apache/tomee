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
package org.apache.openjpa.jdbc.conf;

import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;

import org.apache.openjpa.jdbc.meta.MappingRepository;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.PluginValue;
import org.apache.openjpa.lib.util.J2DoPrivHelper;

import serp.util.Strings;

/**
 * Handles the complex logic of creating a {@link MappingRepository} for
 * a given configuration.
 *
 * @author Marc Prud'hommeaux
 * @nojavadoc
 */
public class MappingRepositoryValue
    extends PluginValue {

    public MappingRepositoryValue(String prop) {
        super(prop, true);
    }

    public Object newInstance(String clsName, Class type,
        Configuration conf, boolean fatal) {
        // since the MappingRepository takes a JDBConfiguration constructor,
        // we need to manually perform the instantiation
        try {
            Class cls = Strings.toClass(clsName,
                AccessController.doPrivileged(
                    J2DoPrivHelper.getClassLoaderAction(type)));        
            return cls.getConstructor(new Class[]{ JDBCConfiguration.class }).
                newInstance(new Object[]{ conf });
        } catch (RuntimeException e) {
            throw e;
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException)
                throw(RuntimeException) e.getTargetException();

            // fall back to default behavior for better error reporting
            return super.newInstance(clsName, type, conf, fatal);
        } catch (Exception e) {
            // fall back to default behavior for better error reporting
            return super.newInstance(clsName, type, conf, fatal);
        }
    }
}
