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

package demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A very simple servlet that is initialized with some example Actor-Movie records.
 *  
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class SimpleApp extends HttpServlet {
    EntityManagerFactory _emf;
    private static String UNIT_NAME = "jestdemo";
    
    @Override 
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        config.getServletContext().log("Initializing persistence unit [" + UNIT_NAME + "]");
        try {
            Map<String,Object> props = new HashMap<String, Object>();
            props.put("openjpa.EntityManagerFactoryPool", "true");
            _emf = Persistence.createEntityManagerFactory(UNIT_NAME, props);
            new DataLoader().populate(_emf.createEntityManager());
        } catch (Exception e) {
            throw new ServletException(e);
        }
        config.getServletContext().log("Initialized with persistence unit [" + UNIT_NAME + "]");
    }
    
    /**
     * The only response by this application is an <code>index.html</code> file.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        OutputStream out = resp.getOutputStream();
        InputStream in = getClass().getResourceAsStream("index.html");
        for (int c = 0; (c = in.read()) != -1;) {
            out.write((char)c);
        }
    }
    
    @Override
    public void destroy() {
        if (_emf != null) {
            _emf.close();
        }
    }
}
