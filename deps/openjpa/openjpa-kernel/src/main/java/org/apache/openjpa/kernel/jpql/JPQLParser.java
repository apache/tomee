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
package org.apache.openjpa.kernel.jpql;

import org.apache.openjpa.kernel.ExpressionStoreQuery;
import org.apache.openjpa.kernel.exps.ExpressionFactory;
import org.apache.openjpa.kernel.exps.ExpressionParser;
import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.UserException;

/**
 * Parser for JPQL queries.
 *
 * @author Marc Prud'hommeaux
 * @nojavadoc
 */
public class JPQLParser
    implements ExpressionParser {

    private static final Localizer _loc =
        Localizer.forPackage(JPQLParser.class);
    public static final String LANG_JPQL = "javax.persistence.JPQL";

    public Object parse(String ql, ExpressionStoreQuery query) {
        if (query.getContext().getParameterDeclaration() != null)
            throw new UserException(_loc.get("param-decs-invalid"));

        try {
        	return new JPQLExpressionBuilder.ParsedJPQL(ql);
        } catch (ParseException e) {
        	throw new ParseException(_loc.get("jpql-parse-error", 
        		ql, e.getMessage()).getMessage(), e);
        }
    }

    public void populate(Object parsed, ExpressionStoreQuery query) {
        if (!(parsed instanceof JPQLExpressionBuilder.ParsedJPQL))
            throw new ClassCastException(parsed == null ? null + ""
                : parsed.getClass().getName());

        ((JPQLExpressionBuilder.ParsedJPQL) parsed).populate(query);
    }

    public QueryExpressions eval(Object parsed, ExpressionStoreQuery query,
        ExpressionFactory factory, ClassMetaData candidate) {
        try {
            return new JPQLExpressionBuilder(factory, query, parsed).
                getQueryExpressions();
        } catch (OpenJPAException ke) {
            throw ke;
        } catch (Exception e) {
            throw new UserException(_loc.get("bad-jpql", parsed), e);
        }
    }

    public Value[] eval(String[] vals, ExpressionStoreQuery query,
        ExpressionFactory factory, ClassMetaData candidate) {
        return null;
    }

    public String getLanguage() {
        return JPQLParser.LANG_JPQL;
    }
}

