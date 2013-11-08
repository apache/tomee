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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Marshals a JPA meta-model in the configured format to the response output stream.
 * 
 * @author Pinaki Poddar
 *
 */
class DomainCommand extends AbstractCommand {
    private static final List<String> _validQualifiers = Arrays.asList("format");
    
    public DomainCommand(JPAServletContext ctx) {
        super(ctx);
    }
    
    protected Collection<String> getValidQualifiers() {
        return _validQualifiers;
    }
    
    protected int getMaximumArguments() {
        return 0;
    }    
    
    public String getAction() {
        return "domain";
    }
    
    public void process() throws ProcessingException, IOException {
        JPAServletContext ctx = getExecutionContext();
        ObjectFormatter<?> formatter = getObjectFormatter();
        ctx.getResponse().setContentType(formatter.getMimeType());
        formatter.writeOut(ctx.getPersistenceContext().getMetamodel(), 
            _loc.get("domain-title").toString(), _loc.get("domain-desc").toString(), ctx.getRequestURI(), 
            ctx.getResponse().getOutputStream());
        
    }
}
