/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import java.util.List;
import java.util.ArrayList;
//TODO does not appear in newly generated classes

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "method-scheduleType", propOrder = {
        "descriptions",
        "ejbName",
        "schedule",
        "method"
        })
public class MethodSchedule {

    @XmlTransient
    protected TextMap description = new TextMap();

    @XmlElement(name = "schedule", required = true)
    protected List<TimerSchedule> schedule;

    @XmlElement(required = true)
    protected NamedMethod method;

    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public MethodSchedule() {
    }

    public MethodSchedule(String className, String ejbName, String methodName, TimerSchedule... schedules) {
        this(ejbName, new NamedMethod(className, methodName), schedules);
    }

    public MethodSchedule(String ejbName, java.lang.reflect.Method method, TimerSchedule... schedules) {
        this(ejbName, new NamedMethod(method), schedules);
    }

    public MethodSchedule(String ejbName, NamedMethod method, TimerSchedule... schedules) {
        this.method = method;
        this.ejbName = ejbName;
        for (TimerSchedule schedule : schedules) {
            getSchedule().add(schedule);
        }
    }

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public List<TimerSchedule> getSchedule() {
        if (schedule == null) {
            schedule = new ArrayList<TimerSchedule>();
        }
        return this.schedule;
    }

    public String getEjbName() {
        return ejbName;
    }

    public void setEjbName(String ejbName) {
        this.ejbName = ejbName;
    }

    public NamedMethod getMethod() {
        return method;
    }

    public void setMethod(NamedMethod method) {
        this.method = method;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public List<TimerSchedule> getAttribute() {
        return getSchedule();
    }

}
