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
package org.apache.openjpa.trader.server;

import java.util.Arrays;
import java.util.List;

/**
 * Adapts a server-side exception to a RuntimeException.
 * This implementation a GWT compiler aware and hence any package dependency will require the
 * source code for this dependent packages. The purpose of this translator is to translate
 * the stack trace of those exceptions, such as <code>org.apache.openjpa.persistence.PersistenceException</code>
 * <em>without</em> bringing in that dependency.
 * 
 * @author Pinaki Poddar
 *
 */
public class ExceptionAdapter {
    static List<String> exceptionTypes = Arrays.asList("org.apache.openjpa.persistence.PersistenceException");
    
    private boolean _printStackTrace;
    
    public void setPrintServerSideStackTrace(boolean flag) {
    	_printStackTrace = flag;
    }
    
    public RuntimeException translate(Throwable t) {
    	if (_printStackTrace)
    		t.printStackTrace();
        Throwable cause = searchForKnownButNonTranslatableException(t);
        if (cause != null) {
            t = cause;
        }
        RuntimeException e = new RuntimeException(t.getMessage());
        e.setStackTrace(t.getStackTrace());
        return e;
    }
    
    private Throwable searchForKnownButNonTranslatableException(Throwable t) {
        if (isAssignable(t.getClass()))
            return t;
        Throwable nested = t.getCause();
        if (nested != null && nested != t) {
            return searchForKnownButNonTranslatableException(nested);
        }
        return null;
    }
    
    private boolean isAssignable(Class<?> t) {
        if (exceptionTypes.contains(t.getName())) {
            return true;
        }
        if (t.getSuperclass() != Object.class) {
            return isAssignable(t.getSuperclass());
        }
        return false;
    }
}
