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

import static org.apache.openjpa.persistence.jest.Constants.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;


/**
 * Represents configuration properties in HTML.
 * 
 * @author Pinaki Poddar
 *
 */
public class PropertiesCommand extends AbstractCommand {
    private static final char DOT = '.';
    
    public PropertiesCommand(JPAServletContext ctx) {
        super(ctx);
    }
    
    protected int getMaximumArguments() {
        return 0;
    }    

    @Override
    public void process() throws ProcessingException, IOException {
        JPAServletContext ctx = getExecutionContext();
        HttpServletResponse response = ctx.getResponse();
        response.setContentType(MIME_TYPE_XML);
        
        Map<String,Object> properties = ctx.getPersistenceContext().getProperties();
        removeBadEntries(properties);
        PropertiesFormatter formatter = new PropertiesFormatter();
        String caption = _loc.get("properties-caption", ctx.getPersistenceUnitName()).toString();
        Document xml = formatter.createXML(caption, "", "", properties);
        formatter.write(xml, response.getOutputStream());
        response.setStatus(HttpURLConnection.HTTP_OK);
    }
    
    private void removeBadEntries(Map<String,Object> map) {
        Iterator<String> keys = map.keySet().iterator();
        for (; keys.hasNext();) {
            if (keys.next().indexOf(DOT) == -1) keys.remove();
        }
    }

    @Override
    protected Format getDefaultFormat() {
        return Format.xml;
    }
}
