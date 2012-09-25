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
package org.apache.webbeans.web.failover;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.FailOverService;

/**
 * Bag which holds all required informations for the fail over.
 */
public class FailOverBag implements Serializable, Externalizable
{
    public static final String SESSION_ATTRIBUTE_NAME = "o.a.owb.FAIL_OVER_BAG";

    private static final Logger LOGGER = WebBeansLoggerFacade.getLogger(FailOverBag.class);
    private static final long serialVersionUID = -6314819837009653190L;

    private transient FailOverService failOverService;

    private Map<String, Object> items;
    private boolean sessionInUse;
    private String sessionId;
    private String jvmId;

    /**
     * Used by serialization.
     */
    public FailOverBag()
    {
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();

        this.failOverService = webBeansContext.getService(FailOverService.class);
        this.items = new HashMap<String, Object>();
    }

    public FailOverBag(String sessionId, String jvmId)
    {
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();

        this.failOverService = webBeansContext.getService(FailOverService.class);
        this.items = new HashMap<String, Object>();
        this.sessionId = sessionId;
        this.jvmId = jvmId;
    }

    public void put(String name, Object item)
    {
        items.put(name, item);
    }

    public Object get(String name)
    {
        return items.get(name);
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        sessionInUse = in.readBoolean();
        sessionId = (String) in.readObject();
        jvmId = (String) in.readObject();

        if (sessionInUse)
        {
            if (LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.log(Level.FINE, "Skip bean de-serialization because session with id [" + sessionId + "] is in use.");
            }

            return;
        }

        byte[] buffer = (byte[]) in.readObject();
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        ObjectInputStream ois = failOverService.getObjectInputStream(bais);

        items = (Map<String, Object>) ois.readObject();

        ois.close();
    }

    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeBoolean(sessionInUse);
        out.writeObject(sessionId);
        out.writeObject(jvmId);

        if (sessionInUse)
        {
            if (LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.log(Level.FINE, "Skip bean serialization because session with id [" + sessionId + "] is in use.");
            }

            return;
        }

        // We could not directly use java object stream since we are using javassist.
        // Serialize the bag by use javassist object stream.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = failOverService.getObjectOutputStream(baos);
        oos.writeObject(items);
        oos.flush();

        out.writeObject(baos.toByteArray());

        oos.close();
        baos.close();
    }

    public boolean isSessionInUse()
    {
        return sessionInUse;
    }

    public void setSessionInUse(boolean sessionInUse)
    {
        this.sessionInUse = sessionInUse;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public String getJvmId()
    {
        return jvmId;
    }

    public void setJvmId(String jvmId)
    {
        this.jvmId = jvmId;
    }
}
