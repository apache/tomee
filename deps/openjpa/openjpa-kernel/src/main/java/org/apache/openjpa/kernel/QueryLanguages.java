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
package org.apache.openjpa.kernel;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.HashMap;
import java.util.Map;

import org.apache.openjpa.kernel.exps.ExpressionParser;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Services;
import org.apache.openjpa.util.InternalException;

/**
 * Constants and utilities for query languages.
 */
public class QueryLanguages {

    public static final String LANG_SQL = "openjpa.SQL";
    public static final String LANG_PREPARED_SQL = "openjpa.prepared.SQL";
    public static final String LANG_METHODQL = "openjpa.MethodQL";

    private static Map _expressionParsers = new HashMap();
    static {
        // Load and cache all the query languages available in the system.
        Class[] classes = Services.getImplementorClasses(
            ExpressionParser.class,
            AccessController.doPrivileged(
                J2DoPrivHelper.getClassLoaderAction(ExpressionParser.class)));
        for (int i = 0; i < classes.length; i++) {
            ExpressionParser ep;
            try {
                ep = (ExpressionParser) AccessController.doPrivileged(
                    J2DoPrivHelper.newInstanceAction(classes[i]));
            } catch (PrivilegedActionException pae) {
                throw new InternalException(pae.getException());
            } catch (InstantiationException e) {
                throw new InternalException(e);
            } catch (IllegalAccessException e) {
                throw new InternalException(e);
            }
            _expressionParsers.put(ep.getLanguage(), ep);
        }
    }

    /**
     * Return the {@link ExpressionParser} for <code>language</code>, or
     * <code>null</code> if no expression parser exists in the system for
     * the specified language.
     */
    public static ExpressionParser parserForLanguage(String language) {
        return (ExpressionParser) _expressionParsers.get(language);
    }
}
