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

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;

/**
 * An operational context combines a {@link OpenJPAEntityManager persistence context} and a HTTP execution
 * context expressed as a {@link HttpServletRequest request} and {@link HttpServletResponse response}. 
 * <br>
 * This context {@link #getAction(String) parses} the HTTP request URL to identity the command and then 
 * {@link #execute() executes} it.
 *  
 * @author Pinaki Poddar
 *
 */
public class JESTContext implements JPAServletContext {
    private final String _unit;
    private final OpenJPAEntityManagerFactory _emf;
    private OpenJPAEntityManager _em;
    private final HttpServletRequest   _request;
    private final HttpServletResponse  _response;
    protected MetaDataRepository       _repos;
    private String _rootResource;
    protected Log _log;
    protected static PrototypeFactory<String,JESTCommand> _cf = new PrototypeFactory<String,JESTCommand>();
    public static final Localizer _loc = Localizer.forPackage(JESTContext.class);
    private static final String ONE_YEAR_FROM_NOW; 
    public static final char QUERY_SEPARATOR = '?'; 
    public static final String CONTEXT_ROOT  = "/";
    public static final String JEST_TEMPLATE  = "jest.html";
    
    
    /**
     * Registers known commands in a {@link PrototypeFactory registry}.
     * 
     */
    static {
        _cf.register("find",  FindCommand.class);
        _cf.register("query", QueryCommand.class);
        _cf.register("domain", DomainCommand.class);
        _cf.register("properties", PropertiesCommand.class);
        
        Calendar now = Calendar.getInstance();
        now.add(Calendar.YEAR, 1);
        ONE_YEAR_FROM_NOW = new Date(now.getTimeInMillis()).toString();
    }
    
    public JESTContext(String unit, OpenJPAEntityManagerFactory emf, HttpServletRequest request, 
        HttpServletResponse response) {
        _unit = unit;
        _emf = emf;
        _request = request;
        _response = response;
        OpenJPAConfiguration conf = _emf.getConfiguration();
        _log = conf.getLog("JEST");
        _repos = conf.getMetaDataRepositoryInstance();
    }
        
    /**
     * Gets the name of the persistence unit.
     */
    public String getPersistenceUnitName() {
        return _unit;
    }
    
    /**
     * Gets the persistence context. The persistence context is lazily constructed because all commands
     * may not need it.  
     */
    public OpenJPAEntityManager getPersistenceContext() {
        if (_em == null) {
            _em = _emf.createEntityManager();
        }
        return _em;
    }
    
    /**
     * Gets the request.
     */
    public HttpServletRequest getRequest() {
        return _request;
    }
    
    /**
     * 
     */
    public String getRequestURI() {
        StringBuffer buf = _request.getRequestURL();
        String query = _request.getQueryString();
        if (!isEmpty(query)) {
            buf.append(QUERY_SEPARATOR).append(query);
        }
        return buf.toString();
        
    }
    
    /**
     * Gets the response.
     */
    public HttpServletResponse getResponse() {
        return _response;
    }
    
    /**
     * Executes the request.
     * <br>
     * Execution starts with parsing the {@link HttpServletRequest#getPathInfo() request path}. 
     * The {@linkplain #getAction(String) first path segment} is interpreted as action key, and
     * if a action with the given key is registered then the control is delegated to the command.
     * The command parses the entire {@link HttpServletRequest request} for requisite qualifiers and
     * arguments and if the parse is successful then the command is 
     * {@linkplain JESTCommand#process() executed} in this context.
     * <br>
     * If path is null, or no  command is registered for the action or the command can not parse
     * the request, then a last ditch attempt is made to {@linkplain #findResource(String) find} a resource. 
     * This fallback lookup is important because the response can contain hyperlinks to stylesheets or
     * scripts. The browser will resolve such hyperlinks relative to the original response. 
     * <br>
     * For example, let the original request URL be:<br>
     * <code>http://host:port/demo/jest/find?type=Actor&Robert</code>
     * <br>
     * The response to this request is a HTML page that contained a hyperlink to <code>jest.css</code> stylesheet
     * in its &lt;head&gt; section.<br>
     * <code>&lt;link ref="jest.css" .....></code> 
     * <br> 
     * The browser will resolve the hyperlink by sending back another request as<br>
     * <code>http://host:port/demo/jest/find/jest.css</code>
     * <br>
     *   
     * @throws Exception
     */
    public void execute() throws Exception {
        String path = _request.getPathInfo();
        if (isContextRoot(path)) {
            getRootResource();
            return;
        }
        String action = getAction(path);
        JESTCommand command = _cf.newInstance(action, this);
        if (command == null) {
            findResource(path.substring(1));
            return;
        }
        try {
            command.parse();
            command.process();
        } catch (ProcessingException e1) {
            throw e1;
        } catch (Exception e2) {
            try {
                findResource(path.substring(action.length()+1));
            } catch (ProcessingException e3) {
                throw e2;
            }
        }
    }
    
    /**
     * Gets the action from the given path.
     * 
     * @param path a string
     * @return if null, returns context root i.e. <code>'/'</code> character. 
     * Otherwise, if the path starts with context root, then returns the substring before the 
     * next <code>'/'</code> character or end of the string, whichever is earlier. 
     * If the path does not start with context root, returns 
     * the substring before the first <code>'/'</code> character or end of the string, whichever is earlier. 
     */
    public static String getAction(String path) {
        if (path == null)
            return CONTEXT_ROOT;
        if (path.startsWith(CONTEXT_ROOT))
            path = path.substring(1); 
        int idx = path.indexOf(CONTEXT_ROOT); 
        return idx == -1 ? path : path.substring(0, idx);
    }
    
    
    public ClassMetaData resolve(String alias) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return _repos.getMetaData(alias, loader, true);
    }
    
    /**
     * A resource is always looked up with respect to this class. 
     * 
     * @param rsrc
     * @throws ProcessingException
     */
    void findResource(String rsrc) throws ProcessingException {
        _response.setHeader("Cache-Control", "public");
        _response.setHeader("Expires", ONE_YEAR_FROM_NOW);
        InputStream in = getClass().getResourceAsStream(rsrc);
        if (in == null) { // try again as a relative path
            if (rsrc.startsWith(CONTEXT_ROOT)) {
                in = getClass().getResourceAsStream(rsrc.substring(1));
            } 
            if (in == null) {
                throw new ProcessingException(this, _loc.get("resource-not-found", rsrc), HTTP_NOT_FOUND);
            }
        }
        try {
            String mimeType = _request.getSession().getServletContext().getMimeType(rsrc);
            if (mimeType == null) {
                mimeType = "application/text";
            }
            _response.setContentType(mimeType);
            OutputStream out = _response.getOutputStream();
            if (mimeType.startsWith("image/")) {
                byte[] b = new byte[1024];
                int i = 0;
                for (int l = 0; (l = in.read(b)) != -1;) {
                    out.write(b, 0, l);
                    i += l;
                }
                _response.setContentLength(i);
            } else {
                for (int c = 0; (c = in.read()) != -1;) {
                    out.write((char)c);
                }
            }
        } catch (IOException e) {
            throw new ProcessingException(this, e, _loc.get("resource-not-found", rsrc), HTTP_NOT_FOUND);
        }
    }
       
    public void log(short level, String message) {
        switch (level) {
            case Log.INFO:  _log.info(message); break;
            case Log.ERROR: _log.fatal(message); break;
            case Log.FATAL: _log.fatal(message); break;
            case Log.TRACE: _log.trace(message); break;
            case Log.WARN:  _log.warn(message); break;
            default: _request.getSession().getServletContext().log(message); 
            
            break;
        }
    }
    
    /**
     * Is this path a context root?
     * @param path
     * @return
     */
    boolean isContextRoot(String path) {
        return (path == null || CONTEXT_ROOT.equals(path));
    }
    
    boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }
    
    /**
     * Root resource is a HTML template with deployment specific tokens such as name of the persistence unit
     * or base url. On first request for this resource, the tokens in the templated HTML file gets replaced  
     * by the actual deployment specific value into a string. This string (which is an entire HTML file)
     * is then written to the response.
     * 
     * @see TokenReplacedStream
     * @throws IOException
     */
    private void getRootResource() throws IOException {
        _response.setHeader("Cache-Control", "public");
        _response.setHeader("Expires", ONE_YEAR_FROM_NOW);
        if (_rootResource == null) {
            String[] tokens = {
                "${persistence.unit}", getPersistenceUnitName(),
                "${jest.uri}",         _request.getRequestURL().toString(),
                "${webapp.name}",     _request.getContextPath().startsWith(CONTEXT_ROOT) 
                                    ? _request.getContextPath().substring(1)
                                    : _request.getContextPath(),
                "${servlet.name}",     _request.getServletPath().startsWith(CONTEXT_ROOT) 
                                    ? _request.getServletPath().substring(1)
                                    : _request.getServletPath(),
                "${server.name}",     _request.getServerName(),
                "${server.port}",     ""+_request.getServerPort(),
                
                "${dojo.base}",     Constants.DOJO_BASE_URL,
                "${dojo.theme}",    Constants.DOJO_THEME,
                
            };
            InputStream in = getClass().getResourceAsStream(JEST_TEMPLATE);
            CharArrayWriter out = new CharArrayWriter();
            new TokenReplacedStream().replace(in, out, tokens);
            _rootResource = out.toString();
        }
        _response.getOutputStream().write(_rootResource.getBytes());
    }
}
