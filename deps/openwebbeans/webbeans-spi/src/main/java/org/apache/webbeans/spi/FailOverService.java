/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.spi;

import javax.enterprise.inject.spi.Bean;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Container provided failover and passivation service.
 */
public interface FailOverService
{
    /**
     * Used for tracking the origin of serialized bean instances.
     * 
     * @return an UUID which is unique for each installation. This might change on restarts.
     */
    public String getJvmId();

    /**
     * @return Whether or not the system supports failover
     */
    public boolean isSupportFailOver();

    /**
     * @return Whether or not the system support passivation
     */
    public boolean isSupportPassivation();

    /**
     * Enable failover support.
     * 
     * @param flag <code>true</code> to enable failover support
     */
    public void enableFailOverSupport(boolean flag);

    /**
     * Enable passivation support.
     * 
     * @param flag <code>true</code> to enable passivation support
     */
    public void enablePassivationSupport(boolean flag);

    /**
     * Inform the service that a session is idle and that beans should be stored for fail over.
     * Invoked when we finish a request.
     * 
     * @param session The {@link HttpSession}.
     */
    public void sessionIsIdle(HttpSession session);

    /**
     * Inform the service that the session will be active.
     * Invoked when a request is received.
     * 
     * @param session The {@link HttpSession}.
     */
    public void sessionIsInUse(HttpSession session);

    /**
     * Informs the service that the session did activate and that beans should be restored.
     * 
     * @param session The {@link HttpSession}.
     */
    public void sessionDidActivate(HttpSession session);

    /**
     * Invoked when the session will passivate and that beans should be stored for passivation.
     * 
     * @param session The {@link HttpSession}.
     */
    public void sessionWillPassivate(HttpSession session);

    /**
     * Container provided object input stream.
     * 
     * Note, the stream should support deserializing javassist objects.
     * 
     * @return custom object input stream.
     */
    public ObjectInputStream getObjectInputStream(InputStream in) throws IOException;

    /**
     * Container provided object output stream.
     * 
     * Note, the stream should support serializing javassist objects.
     * 
     * @return custom object output stream.
     */
    public ObjectOutputStream getObjectOutputStream(OutputStream out) throws IOException;

    /**
     * Container provided custom handler for serialize / deserialize a resource
     * bean. Add clean up code in this method will allow OWB to override default
     * resource bean passivation behavior.
     * 
     * Note, in the method, a container may first invoke the application
     * provided handler(@See SerializationHandler) if it is configured.
     * 
     * @param bean The resource bean.
     * @param resourceObject The resource bean instance
     * @param in The input object stream
     * @param out The output object stream
     * 
     * @return {@link #NOT_HANDLED} if not handled by handler.
     */
    public Object handleResource(
            Bean<?> bean,
            Object resourceObject,
            ObjectInput in,
            ObjectOutput out
    );

    /**
     * Returned, if container or application does not handle the resource object
     * in the handleResource() method.
     */
    public final static Object NOT_HANDLED = new Object();

}
