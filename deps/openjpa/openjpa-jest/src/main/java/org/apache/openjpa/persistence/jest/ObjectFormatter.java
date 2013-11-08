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
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;

import javax.persistence.metamodel.Metamodel;
import javax.servlet.http.HttpServletResponse;

import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * A parameterized interface defines the protocol for converting {@link OpenJPAStateManager managed} persistence 
 * instances or a persistent {@link Metamodel domain model} into a form suitable for transport to a language-neutral 
 * client such as an web browser.
 * <p>
 * The interface prefers that the resultant resource as a <em>complete</em> representation i.e. all the references 
 * contained in the resource can be resolved within the same resource itself. As the intended recipient of this 
 * resource is a remote client, an <em>incomplete</em> resource will require the client to request further for
 * any (unresolved) reference resulting in a <em>chatty</em> protocol.
 * <p>
 * This interface also defines methods for writing the representation into an output stream e.g. 
 * {@link HttpServletResponse#getOutputStream() response output stream} of a HTTP Servlet.
 * <p>
 * Implementation Note:  Each concrete formatter type is registered with {@linkplain PrototypeFactory factory}
 * that requires the implementation to have a no-argument constructor. 
 * 
 * @param <T> the type of encoded output
 * 
 * @author Pinaki Poddar
 *
 */
public interface ObjectFormatter<T> {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
    
    /**
     * Gets the mime type produced by this formatter.
     */
    public String getMimeType();
    
    /**
     * Encode the {@link Closure persistent closure} of the given collection of managed instances as a 
     * resource e.g a XML or HTML document or an interactive document with JavaScript or a JSON array.
     * Exact nature of the output type is the generic parameter of this interface.
     * 
     * @param objs a collection of managed instances
     * @param model domain model
     * 
     * @return an encoded object e.g. a XML or HTML Document or a JSON object. 
     */
    public T encode(Collection<OpenJPAStateManager> objs, Metamodel model);
    
    /**
     * Encode the given domain model in to a object.
     * 
     * @param model a meta-model of managed types
     * 
     * @return an encoded object e.g. a XML or HTML Document or a JSON object. 
     */
    public T encode(Metamodel model);
    
    /**
     * Encodes the {@link Closure persistent closure} of the given collection of objects, then write it into 
     * the given output stream. 
     * 
     * @param objs the collection of objects to be formatted.
     * @param model a meta-model of managed types, provided for easier introspection if necessary
     * @param title TODO
     * @param desc TODO
     * @param uri TODO
     * @param writer a text-oriented output stream
     * @throws IOException
     */
    public T writeOut(Collection<OpenJPAStateManager> objs, Metamodel model,
        String title, String desc, String uri, OutputStream out) throws IOException;
    
    /**
     * Encodes the given domain model, then write it into the given output stream.
     * 
     * @param model a meta-model of managed types
     * @param writer a text-oriented output stream
     * 
     * @throws IOException
     */
    public T writeOut(Metamodel model, String title, String desc, String uri, OutputStream out) throws IOException;
}
