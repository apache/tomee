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

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


/**
 * Interface for JEST commands. A JEST command denotes a JPA operation such as <code>find</code>,
 * <code>query</code> or <code>domain</code>. Besides signifying a JPA operation, a command may have
 * zero or more qualifiers and arguments. 
 * <br>
 * A qualifier qualifies the action to be performed. For example, a <code>query</code> command may be qualified
 * to return a single instance as its result, or limit its result to first 20 instances etc.
 * <br>
 * An argument is an argument to the target JPA method. For example, <code>find</code> command has
 * arguments for the type of the instance and the primary key. A <code>query</code> command has the
 * query string as its argument.
 * <p>
 * A concrete command instance is an outcome of parsing a {@link HttpServletRequest request}. 
 * The {@link HttpServletRequest#getPathInfo() path} segments are parsed for qualifiers. 
 * The {@link HttpServletRequest#getQueryString() query string} is parsed for the arguments. 
 * <p>
 * A JEST command often attaches special semantics to a standard URI syntax. For example, all JEST
 * URI enforces that the first segment of a servlet path denotes the command moniker e.g. the URI<br>
 * <code>http://www.jpa.com/jest/find/plan=myPlan?type=Person&1234</code><br>
 * with context root <code>http://www.jpa.com/jest</code> has the servlet path <code>/find/plan=myPlan</code>
 * and query string <code>type=Person&1234</code>.
 * <br>
 * The first path segment <code>find</code> will determine that the command is to <em>find</em> a 
 * persistent entity of type <code>Person</code> and primary key <code>1234</code> using a fetch plan
 * named <code>myPlan</code>.
 *  
 * @author Pinaki Poddar
 *
 */
public interface JESTCommand {
    /**
     * Supported format monikers.
     */
    public static enum Format {xml, json};
    
    /**
     * Get the execution context of this command.
     * 
     * @return the execution context. never null.
     */
    public JPAServletContext getExecutionContext();

    /**
     * Parse the given request to populate qualifiers and parameters of this command.
     * A command can interpret and consume certain path segments or parameters of the
     * original request. During {@link #process(ServletRequest, ServletResponse, JPAServletContext) processing}
     * phase, the parameters and qualifiers are accessed from the parsed command itself rather than
     * from the original HTTP request.
     */
    public void parse() throws ProcessingException;
    
    /**
     * Process the given request and write the output on to the given response in the given context.
     * @throws ProcessingException 
     * 
     */
    public void process() throws ProcessingException, IOException;
    

    /**
     * Get this command's arguments. 
     * 
     * @exception IllegalStateException if accessed prior to parsing.
     */
    public Map<String,String> getArguments();
    
    /**
     * Get the value of this command's argument of the given name. 
     * 
     * @return null if the argument does not exist.
     * 
     * @exception IllegalStateException if accessed prior to parsing.
     */
    public String getArgument(String key);
    
    
    /**
     * Affirm this command contains an argument of the given name. 
     * 
     * @exception IllegalStateException if accessed prior to parsing.
     */
    public boolean hasArgument(String key);

    /**
     * Get this command's qualifiers. 
     * 
     * @exception IllegalStateException if accessed prior to parsing.
     */
    public Map<String,String> getQualifiers();
    
    /**
     * Get the value of this command's qualifier of the given name. 
     * 
     * @return null if the qualifier does not exist.
     * 
     * @exception IllegalStateException if accessed prior to parsing.
     */
    public String getQualifier(String key);
    
    /**
     * Affirm this command contains an qualifier of the given name. 
     * 
     * @exception IllegalStateException if accessed prior to parsing.
     */
    public boolean hasQualifier(String key);
    
}
