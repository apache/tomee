index-group=Unrevised
type=page
status=published
title=Resources Declared in Webapp
~~~~~~

*Help us document this example! Click the blue pencil icon in the upper right to edit this page.*

## Manager

    package org.superbiz.bean;
    
    import org.superbiz.resource.ManagerResource;
    
    import javax.annotation.Resource;
    import javax.ejb.Singleton;
    
    @Singleton
    public class Manager {
        @Resource(name = "My Manager Team", type = ManagerResource.class)
        private ManagerResource resource;
    
        public String work() {
            return "manage a resource of type " + resource.resourceType();
        }
    }

## ManagerResource

    package org.superbiz.resource;
    
    public class ManagerResource {
        public String resourceType() {
            return "team";
        }
    }

## ManagerServlet

    package org.superbiz.servlet;
    
    import org.superbiz.bean.Manager;
    
    import javax.ejb.EJB;
    import javax.servlet.ServletException;
    import javax.servlet.annotation.WebServlet;
    import javax.servlet.http.HttpServlet;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import java.io.IOException;
    
    @WebServlet(name = "manager servlet", urlPatterns = "/")
    public class ManagerServlet extends HttpServlet {
        @EJB
        private Manager manager;
    
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.getOutputStream().print(manager.work());
        }
    }

## ejb-jar.xml

    <!--
      Licensed to the Apache Software Foundation (ASF) under one or more
      contributor license agreements.  See the NOTICE file distributed with
      this work for additional information regarding copyright ownership.
      The ASF licenses this file to You under the Apache License, Version 2.0
      (the "License"); you may not use this file except in compliance with
      the License.  You may obtain a copy of the License at
    
          http://www.apache.org/licenses/LICENSE-2.0
    
      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.
    -->
    <ejb-jar/>
    

## service-jar.xml

    <ServiceJar>
      <ServiceProvider id="ManagerResource" service="Resource"
                       type="org.superbiz.resource.ManagerResource"
                       class-name="org.superbiz.resource.ManagerResource"/>
    </ServiceJar>
    

## resources.xml

    <resources>
      <Resource id="My Manager Team" type="org.superbiz.resource.ManagerResource" provider="org.superbiz#ManagerResource"/>
    </resources>
    

## web.xml

    <!--
    
        Licensed to the Apache Software Foundation (ASF) under one or more
        contributor license agreements.  See the NOTICE file distributed with
        this work for additional information regarding copyright ownership.
        The ASF licenses this file to You under the Apache License, Version 2.0
        (the "License"); you may not use this file except in compliance with
        the License.  You may obtain a copy of the License at
    
           http://www.apache.org/licenses/LICENSE-2.0
    
        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
    -->
    <web-app version="3.0" xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"/>
    
