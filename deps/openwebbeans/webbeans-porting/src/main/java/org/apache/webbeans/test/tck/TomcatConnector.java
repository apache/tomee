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
package org.apache.webbeans.test.tck;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.Containers;
import org.jboss.testharness.spi.helpers.AbstractContainerConnector;

//NOTE : This code is copied from RI TCK. We have to extend
//this class but its jar file contains jboss-test-harness.properties
//that conflict with our provided jboss-test-harness.properties. therefore
//we remove jar from dependency and copied this class and extends it with
//our own version.
public class TomcatConnector extends AbstractContainerConnector implements Containers
{

    private Logger logger = WebBeansLoggerFacade.getLogger(TomcatConnector.class);
    
   private static final String SERVER_HOME_PROPERTY_NAME = "tomcat.home";

   private String binDirectory; 
   private final File tmpdir;
   private final HttpClient client;

   private DeploymentException deploymentException;

   public TomcatConnector() throws IOException
   {
      logger.log(Level.INFO, "You must add the the tests/secret user to Tomcat, for example, in $CATALINA_BASE/conf/tomcat-users.xml add " +
                  "<user name=\"tests\" password=\"secret\" roles=\"standard,manager\" />");
      tmpdir = new File(System.getProperty("java.io.tmpdir"), "org.jboss.webbeans.tck.integration.jbossas");
      tmpdir.mkdir();
      tmpdir.deleteOnExit();
      client = new HttpClient();
      client.getParams().setAuthenticationPreemptive(true);
      Credentials credentials = new UsernamePasswordCredentials("tests", "secret");
      client.getState().setCredentials(new AuthScope(null, 8080, null), credentials);
   }

   @Override
   protected String getServerHomePropertyName()
   {
      return SERVER_HOME_PROPERTY_NAME;
   }

   @Override
   protected void shutdownServer() throws IOException
   {
      launch(getBinDirectory(), "shutdown", "");
   }

   @Override
   protected void startServer() throws IOException
   {
      launch(getBinDirectory(), "startup", "");
   }

   protected String getBinDirectory()
   {
      if (binDirectory == null)
      {
         binDirectory = new File(getServerDirectory() + "/bin").getPath();
      }
      return binDirectory;
   }

   @Override
   protected String getLogName()
   {
      return "tomcat.log";
   }

   public boolean deploy(InputStream stream, String name) throws IOException
   {
      String deployUrl = getManagerUrl("deploy", "path=/" + getContextName(name), "update=true");
      PutMethod put = new PutMethod(deployUrl);
      put.setRequestEntity(new InputStreamRequestEntity(stream));
      try
      {
         int status = client.executeMethod(put);
         if (status != HttpURLConnection.HTTP_OK)
         {
            deploymentException = getDeploymentExceptionTransformer().transform(new DeploymentException(new String(put.getResponseBody())));
            return false;
         }
         return true;
      }
      finally
      {
         put.releaseConnection();
      }
   }

   public DeploymentException getDeploymentException()
   {
      return deploymentException;
   }

   public void undeploy(String name) throws IOException
   {
      String deployUrl = getManagerUrl("undeploy", "path=/" + getContextName(name));
      HttpMethod get = new GetMethod(deployUrl);
      try
      {
         int status = client.executeMethod(get);
         if (status != HttpURLConnection.HTTP_OK)
         {
            throw new IllegalStateException(new String(get.getResponseBody()));
         }
      }
      finally
      {
         get.releaseConnection();
         ManagersImpl.cleanUp();
      }
   }

   protected String getManagerUrl(String command, String... parameters)
   {
      String url = getHttpUrl() + "manager/" + command ;
      for (int i = 0; i < parameters.length; i ++)
      {
         String parameter = parameters[i];
         if (i == 0)
         {
            url += "?" + parameter;
         }
         else
         {
            url += "&" + parameter;
         }
      }
      return url;
   }

   protected String getContextName(String name)
   {
      return name.substring(0, name.length() - 4);
   }

}
