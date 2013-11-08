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

package org.apache.openjpa.persistence.jest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

/**
 * An operating context provides a {@link EntityManage persistence context} and utility functions within
 * which all JEST commands execute.
 *  
 * @author Pinaki Poddar
 *
 */
public interface JPAServletContext {
    /**
     * Get the persistence context of the operational context.
     */
    public OpenJPAEntityManager getPersistenceContext();
    
    /**
     * Get the persistence unit name.
     */
    public String getPersistenceUnitName();
    
    /**
     * Get the HTTP Request.
     */
    public HttpServletRequest getRequest();
    
    /**
     * Get the HTTP Response.
     */
    public HttpServletResponse getResponse();
    
    /**
     * Get the requested URI. 
     * @return
     */
    public String getRequestURI();
    
    /**
     * Resolve the given alias to meta-data of the persistent type.
     * @param alias a moniker for the Java type. It can be fully qualified type name or entity name
     * or simple name of the actual persistent Java class.
     * 
     * @return meta-data for the given name. 
     * @exception raises runtime exception if the given name can not be identified to a persistent
     * Java type.
     */
    public ClassMetaData resolve(String alias);
    
    /**
     * Logging message.
     * @param level OpenJPA defined {@link Log#INFO log levels}. Invalid levels will print the message on console.
     * @param message a printable message.
     */
    public void log(short level, String message);
}
