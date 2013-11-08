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

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static org.apache.openjpa.persistence.jest.Constants.QUALIFIER_FORMAT;
import static org.apache.openjpa.persistence.jest.Constants.QUALIFIER_PLAN;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.kernel.BrokerImpl;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.persistence.FetchPlan;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

/**
 * The abstract base class for all commands available to JEST.
 * 
 * @author Pinaki Poddar
 *
 */
abstract class AbstractCommand implements JESTCommand {
    public static final char EQUAL                    = '=';
    public static final String PATH_SEPARATOR         = "/";
    public static final Collection<String> EMPTY_LIST = Collections.emptySet();
    protected ObjectFormatter<?> _formatter;

    private Map<String, String> _qualifiers = new HashMap<String, String>();
    private Map<String, String> _args = new HashMap<String, String>();
    private Map<String, String> _margs = new HashMap<String, String>();
    protected final JPAServletContext _ctx;
    
    private static PrototypeFactory<Format,ObjectFormatter<?>> _formatterFactory = 
        new PrototypeFactory<Format,ObjectFormatter<?>>();
    protected static Localizer _loc = Localizer.forPackage(AbstractCommand.class);
    
    static {
        _formatterFactory.register(Format.xml,  XMLFormatter.class);
        _formatterFactory.register(Format.json, JSONObjectFormatter.class);
    }
    
    protected AbstractCommand(JPAServletContext ctx) {
        _ctx = ctx;
    }
    
    public JPAServletContext getExecutionContext() {
        return _ctx;
    }
    
    public String getMandatoryArgument(String key) {
        return get(key, _margs);
    }
    
    public String getArgument(String key) {
        return get(key, _args);
    }
    
    public boolean hasArgument(String key) {
        return has(key, _args);
    }
    
    public Map<String, String> getArguments() {
        return _args;
    }
    
    public String getQualifier(String key) {
        return get(key, _qualifiers);
    }
    
    public boolean hasQualifier(String key) {
        return has(key, _qualifiers);
    }
    
    protected boolean isBooleanQualifier(String key) {
        if (hasQualifier(key)) {
            Object value = getQualifier(key);
            return value == null || "true".equalsIgnoreCase(value.toString());
        }
        return false;
    }
    
    public Map<String, String> getQualifiers() {
        return _qualifiers;
    }
    
    /**
     * Parses HTTP Request for the qualifier and argument of a command.
     * <br>
     * Each servlet path segment, except the first (which is the command name itself), is a qualifier.
     * Each qualifier can be either a key or a key-value pair separated by a = sign.
     * <br>
     * Each request parameter key-value pair is an argument. A concrete command may specify mandatory
     * arguments (e.g. <code>type</code> must be mandatory argument for <code>find</code> command,
     * or <code>q</code> for <code>query</code>. The mandatory arguments, if any, are <em>not</em> captured
     * in the argument list. 
     * <br>
     * The qualifiers and arguments are immutable after parse.
     */
    public void parse() throws ProcessingException {
        HttpServletRequest request = _ctx.getRequest();
        String path = request.getPathInfo();
        if (path != null) {
            path = path.substring(1);
            String[] segments = path.split(PATH_SEPARATOR);
            for (int i = 1; i < segments.length; i++) {
                String segment = segments[i];
                int idx = segment.indexOf(EQUAL);
                if (idx == -1) {
                    _qualifiers.put(segment, null);
                } else {
                    _qualifiers.put(segment.substring(0, idx), segment.substring(idx+1));
                }
            }
        }
        _qualifiers = Collections.unmodifiableMap(_qualifiers);
        
        Enumeration<?> names = request.getParameterNames();
        Collection<String> mandatoryArgs = getMandatoryArguments();
        
        while (names.hasMoreElements()) {
            String key = names.nextElement().toString();
            if (key.startsWith("dojo.")) continue;
            put(key, request.getParameter(key), mandatoryArgs.contains(key) ? _margs : _args);
        }
        _args = Collections.unmodifiableMap(_args);
        _margs = Collections.unmodifiableMap(_margs);
        
        validate();
    }
    
    /**
     * Gets the mandatory arguments.
     * 
     * @return empty list by default.
     */
    protected Collection<String> getMandatoryArguments() {
        return EMPTY_LIST;
    }
    
    /**
     * Gets the minimum number of arguments excluding the mandatory arguments.
     * 
     * @return zero by default.
     */
    protected int getMinimumArguments() {
        return 0;
    }
    
    /**
     * Gets the maximum number of arguments excluding the mandatory arguments.
     * 
     * @return Integer.MAX_VALUE by default.
     */
    protected int getMaximumArguments() {
        return Integer.MAX_VALUE;
    }
    
    protected Format getDefaultFormat() {
        return Format.xml;
    }
    
    /**
     * Gets the valid qualifiers.
     * 
     * @return empty list by default.
     */
    protected Collection<String> getValidQualifiers() {
        return EMPTY_LIST;
    }
    
    /**
     * Called post-parse to validate this command has requisite qualifiers and arguments.
     */
    protected void validate() {
        HttpServletRequest request = _ctx.getRequest();
        Collection<String> validQualifiers = getValidQualifiers();
        for (String key : _qualifiers.keySet()) {
            if (!validQualifiers.contains(key)) {
                throw new ProcessingException(_ctx,_loc.get("parse-invalid-qualifier", this, key, validQualifiers),
                    HTTP_BAD_REQUEST);
            }
        }
        Collection<String> mandatoryArgs = getMandatoryArguments();
        for (String key : mandatoryArgs) {
            if (request.getParameter(key) == null) {
                throw new ProcessingException(_ctx, _loc.get("parse-missing-mandatory-argument", this, key,  
                    request.getParameterMap().keySet()), HTTP_BAD_REQUEST);
            }
        }
        if (_args.size() < getMinimumArguments()) {
            throw new ProcessingException(_ctx, _loc.get("parse-less-argument", this, _args.keySet(),  
                getMinimumArguments()), HTTP_BAD_REQUEST);
        }
        if (_args.size() > getMaximumArguments()) {
            throw new ProcessingException(_ctx, _loc.get("parse-less-argument", this, _args.keySet(),  
                getMinimumArguments()), HTTP_BAD_REQUEST);
        }
    }
    
    private String get(String key, Map<String,String> map) {
        return map.get(key);
    }
    
    private String put(String key, String value, Map<String,String> map) {
        return map.put(key, value);
    }
    
    private boolean has(String key, Map<String,String> map) {
        return map.containsKey(key);
    }
    
    public ObjectFormatter<?> getObjectFormatter() {
        if (_formatter == null) {
            String rformat = getQualifier(QUALIFIER_FORMAT);
            Format format = null;
            if (rformat == null) {
                format = getDefaultFormat();
            } else {
                try {
                    format = Format.valueOf(rformat);
                } catch (Exception e) {
                    throw new ProcessingException(_ctx, _loc.get("format-not-supported", new Object[]{format, 
                        _ctx.getRequest().getPathInfo(), _formatterFactory.getRegisteredKeys()}), HTTP_BAD_REQUEST);
                }
            }
            _formatter = _formatterFactory.newInstance(format);
            if (_formatter == null) {
                throw new ProcessingException(_ctx, _loc.get("format-not-supported", new Object[]{format, 
                    _ctx.getRequest().getPathInfo(), _formatterFactory.getRegisteredKeys()}), HTTP_BAD_REQUEST);
            }
        }
        return _formatter;
    }

    protected OpenJPAStateManager toStateManager(Object obj) {
        if (obj instanceof OpenJPAStateManager)
            return (OpenJPAStateManager)obj;
        if (obj instanceof PersistenceCapable) {
            return (OpenJPAStateManager)((PersistenceCapable)obj).pcGetStateManager();
        }
        return null;
    }
    
    protected List<OpenJPAStateManager> toStateManager(Collection<?> objects) {
        List<OpenJPAStateManager> sms = new ArrayList<OpenJPAStateManager>();
        for (Object o : objects) {
            OpenJPAStateManager sm = toStateManager(o);
            if (sm != null) sms.add(sm);
        }
        return sms;
    }
    
    protected void pushFetchPlan(Object target) {
        if (!hasQualifier(QUALIFIER_PLAN))
            return;
        OpenJPAEntityManager em = _ctx.getPersistenceContext();
        FetchPlan plan = em.pushFetchPlan();
        BrokerImpl broker = (BrokerImpl)JPAFacadeHelper.toBroker(em);
        if (target instanceof OpenJPAEntityManager) {
            broker.setCacheFinderQuery(false);
        } else if (target instanceof OpenJPAQuery) {
            broker.setCachePreparedQuery(false);
        }
        
        String[] plans = getQualifier(QUALIFIER_PLAN).split(",");
        for (String p : plans) {
            p = p.trim();
            if (p.charAt(0) == '-') {
                plan.removeFetchGroup(p.substring(1));
            } else {
                plan.addFetchGroup(p);
            }
        }
    }
    
    protected void popFetchPlan(boolean finder) {
        if (!hasQualifier(QUALIFIER_PLAN))
            return;
        OpenJPAEntityManager em = _ctx.getPersistenceContext();
        BrokerImpl broker = (BrokerImpl)JPAFacadeHelper.toBroker(em);
        if (finder) {
            broker.setCacheFinderQuery(false);
        } else {
            broker.setCachePreparedQuery(false);
        }
    }

    protected void debug(HttpServletRequest request, HttpServletResponse response, JPAServletContext ctx) 
        throws IOException {
        response.setContentType(Constants.MIME_TYPE_PLAIN);
        PrintWriter writer = response.getWriter();
        
        writer.println("URI             = [" + request.getRequestURI() + "]");
        writer.println("URL             = [" + request.getRequestURL() + "]");
        writer.println("Servlet Path    = [" + request.getServletPath() + "]"); // this is one we need
        writer.println("Context Path    = [" + request.getContextPath() + "]");
        writer.println("Translated Path = [" + request.getPathTranslated() + "]");// not decoded
        writer.println("Path Info       = [" + request.getPathInfo() + "]");// decoded
        String query = request.getQueryString();
        if (query != null) {
            query = URLDecoder.decode(request.getQueryString(),"UTF-8");
        }
        writer.println("Query           = [" + query + "]"); // and this one
        int i = 0;
        for (Map.Entry<String, String> e : _qualifiers.entrySet()) {
            writer.println("Qualifier [" + i + "] = [" + e.getKey() + ": " + e.getValue() + "]");
            i++;
        }
        i = 0;
        for (Map.Entry<String, String> e : _args.entrySet()) {
            writer.println("Parameter [" + i + "] = [" + e.getKey() + ": " + e.getValue() + "]");
            i++;
        }
    }
    
}
