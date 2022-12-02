/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.util;

/**
 * Contains Logger categories used in OpenEJB. Be careful when adding new Categories. For example, if a new Category
 * named OpenEJB.shutdown needs to be added, then the following is not a recommended way
 * public static final LogCategory OPENEJB_SHUTDOWN = new LogCategory("OpenEJB.shutdown");
 * The above is not recommended because the above logger has a parent logger in OpenEJB. If we change the Parent logger
 * category i.e. lets say to OPENEJB (all uppercase), then to maintain the parent-child relationship, we will need
 * to change other loggers too. For example, we will not need to change OPENEJB_STARTUP and OPENEJB_SERVER because
 * of the way they are defined (using OPENEJB.name as a prefix).
 * A better way of adding the Category would be
 * public static final LogCategory OPENEJB_SHUTDOWN = new LogCategory( OPENEJB.name + ".shutdown");
 */
public final class LogCategory {

    private final String name;
    public static final LogCategory OPENEJB = new LogCategory("OpenEJB");
    public static final LogCategory OPENEJB_ADMIN = OPENEJB.createChild("admin");
    public static final LogCategory OPENEJB_STARTUP = OPENEJB.createChild("startup");
    public static final LogCategory OPENEJB_STARTUP_CONFIG = OPENEJB_STARTUP.createChild("config");
    public static final LogCategory OPENEJB_STARTUP_VALIDATION = OPENEJB_STARTUP.createChild("validation");
    public static final LogCategory OPENEJB_SERVER = OPENEJB.createChild("server");
    public static final LogCategory OPENEJB_SERVER_REMOTE = OPENEJB_SERVER.createChild("remote");
    public static final LogCategory OPENEJB_SECURITY = OPENEJB.createChild("security");
    public static final LogCategory OPENEJB_RESOURCE_JDBC = OPENEJB.createChild("resource.jdbc");
    public static final LogCategory OPENEJB_CONNECTOR = OPENEJB.createChild("connector");
    public static final LogCategory OPENEJB_DEPLOY = OPENEJB.createChild("deploy");
    public static final LogCategory OPENEJB_HSQL = OPENEJB.createChild("hsql");
    public static final LogCategory OPENEJB_WS = OPENEJB.createChild("ws");
    public static final LogCategory OPENEJB_RS = OPENEJB.createChild("rs");
    public static final LogCategory OPENEJB_JPA = OPENEJB.createChild("jpa");
    public static final LogCategory OPENEJB_CDI = OPENEJB.createChild("cdi");
    public static final LogCategory TRANSACTION = new LogCategory("Transaction");
    public static final LogCategory ACTIVEMQ = new LogCategory("org.apache.activemq");
    public static final LogCategory GERONIMO = new LogCategory("org.apache.geronimo");
    public static final LogCategory OPENJPA = new LogCategory("openjpa");
    public static final LogCategory CORBA_ADAPTER = new LogCategory("CORBA-Adapter");
    public static final LogCategory AXIS = new LogCategory("axis");
    public static final LogCategory AXIS2 = new LogCategory("axis");
    public static final LogCategory CXF = new LogCategory("cxf");
    public static final LogCategory TIMER = new LogCategory("Timer");
    public static final LogCategory HTTPSERVER = OPENEJB_SERVER.createChild("http");
    public static final LogCategory SERVICEPOOL = OPENEJB_SERVER.createChild("pool");
    public static final LogCategory OPENEJB_SQL = OPENEJB.createChild("sql");
    public static final LogCategory MONITORING = OPENEJB.createChild("monitoring");
    public static final LogCategory MICROPROFILE = OPENEJB.createChild("microprofile");

    private LogCategory(final String name) {
        this.name = name == null ? "" : name;
    }

    public String getName() {
        return name;
    }

    /**
     * Creates a child category of this category. <B>Use this method sparingly</B>. This method is to be used in only those circumstances where the name of the
     * category is not known upfront and is a derived name. If you know the name of the category, it is highly recommended to add a static final field
     * of type LogCategory in this class
     *
     * @param child String
     * @return - LogCategory
     */
    public LogCategory createChild(final String child) {
        return new LogCategory(this.name + "." + child);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && name.equals(LogCategory.class.cast(o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
