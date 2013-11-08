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
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.util.ApplicationIds;


/**
 * @author Pinaki Poddar
 *
 */
class FindCommand extends AbstractCommand {
    private static final String ARG_TYPE = "type";
    private static final List<String> _mandatoryArgs   = Arrays.asList(ARG_TYPE);
    private static final List<String> _validQualifiers = Arrays.asList("format", "plan");
    
    public FindCommand(JPAServletContext ctx) {
        super(ctx);
    }
    
    @Override
    protected Collection<String> getMandatoryArguments() {
        return _mandatoryArgs;
    }
    
    @Override
    protected int getMinimumArguments() {
        return 1;
    }
    
    protected Collection<String> getValidQualifiers() {
        return _validQualifiers;
    }

    @Override
    public void process() throws ProcessingException {
        JPAServletContext ctx = getExecutionContext();
        OpenJPAEntityManager em = ctx.getPersistenceContext();
        String type = getMandatoryArgument(ARG_TYPE);
        ClassMetaData meta = ctx.resolve(type);
        Map<String,String> parameters = getArguments();
        Object[] pks = new Object[parameters.size()];
        Iterator<Map.Entry<String,String>> params = parameters.entrySet().iterator();
        for (int i = 0; i < parameters.size(); i++) {
            pks[i] = params.next().getKey();
        }
        Object oid = ApplicationIds.fromPKValues(pks, meta);
        pushFetchPlan(em);
        try {
            Object pc = em.find(meta.getDescribedType(), oid); 
            if (pc != null) {
                OpenJPAStateManager sm = toStateManager(pc);
                ObjectFormatter<?> formatter = getObjectFormatter();
                ctx.getResponse().setContentType(formatter.getMimeType());
                try {
                    formatter.writeOut(Collections.singleton(sm), em.getMetamodel(), 
                        _loc.get("find-title").toString(), _loc.get("find-desc").toString(), ctx.getRequestURI(), 
                        ctx.getResponse().getOutputStream());
                } catch (IOException e) {
                    throw new ProcessingException(ctx, e);
                }
            } else {
                throw new ProcessingException(ctx, _loc.get("entity-not-found", type, Arrays.toString(pks)), 
                    HttpURLConnection.HTTP_NOT_FOUND);
            }
        } finally {
            popFetchPlan(true);
        }
    }
}
