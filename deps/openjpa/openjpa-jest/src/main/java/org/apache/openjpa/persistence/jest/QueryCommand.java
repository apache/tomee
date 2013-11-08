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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static org.apache.openjpa.persistence.jest.Constants.*;

import javax.persistence.Query;

import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;;

/**
 * Executes query.
 * 
 * @author Pinaki Poddar
 *
 */
class QueryCommand extends AbstractCommand {
    private static final String ARG_QUERY    = "q";
    public static final String QUALIFIER_MAXRESULT   = "max";
    public static final String QUALIFIER_FIRSTRESULT = "first";
    public static final String QUALIFIER_NAMED       = "named";
    public static final String QUALIFIER_SINGLE      = "single";
    private static final List<String> _mandatoryArgs   = Arrays.asList(ARG_QUERY);
    private static final List<String> _validQualifiers = Arrays.asList(
        QUALIFIER_FORMAT, QUALIFIER_PLAN, QUALIFIER_NAMED, QUALIFIER_SINGLE, 
        QUALIFIER_FIRSTRESULT, QUALIFIER_MAXRESULT);
    
    public QueryCommand(JPAServletContext ctx) {
        super(ctx);
    }

    @Override
    protected Collection<String> getMandatoryArguments() {
        return _mandatoryArgs;
    }
    
    @Override
    protected int getMinimumArguments() {
        return 0;
    }
    
    protected Collection<String> getValidQualifiers() {
        return _validQualifiers;
    }

    @Override
    public void process() throws ProcessingException {
        JPAServletContext ctx = getExecutionContext();
        String spec = getMandatoryArgument(ARG_QUERY);
        OpenJPAEntityManager em = ctx.getPersistenceContext();
        try {
            Query query = isBooleanQualifier(QUALIFIER_NAMED) ? em.createNamedQuery(spec) : em.createQuery(spec);
            if (hasQualifier(QUALIFIER_FIRSTRESULT)) 
                query.setFirstResult(Integer.parseInt(getQualifier(QUALIFIER_FIRSTRESULT)));
            if (hasQualifier(QUALIFIER_MAXRESULT)) 
                query.setMaxResults(Integer.parseInt(getQualifier(QUALIFIER_MAXRESULT)));
            pushFetchPlan(query);
            
            Map<String, String> args = getArguments();
            for (Map.Entry<String, String> entry : args.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
            getObjectFormatter()
                .writeOut(toStateManager(isBooleanQualifier(QUALIFIER_SINGLE) 
                 ? Collections.singleton(query.getSingleResult()) : query.getResultList()), 
                 em.getMetamodel(), 
                 _loc.get("query-title").toString(), _loc.get("query-desc").toString(), ctx.getRequestURI(), 
                 ctx.getResponse().getOutputStream());
        } catch (ArgumentException e1) {
            throw new ProcessingException(ctx, e1, _loc.get("query-execution-error", spec), HTTP_BAD_REQUEST);
        } catch (Exception e) {
            throw new ProcessingException(ctx, e, _loc.get("query-execution-error", spec));
        } finally {
            popFetchPlan(false);
        }
    }
}
