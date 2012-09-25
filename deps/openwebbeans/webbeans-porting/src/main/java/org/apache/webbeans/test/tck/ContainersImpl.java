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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.Containers;

public class ContainersImpl extends TomcatConnector implements Containers
{
    private HttpClient client = null;
    
    private DeploymentException excepton = null;
    
    public ContainersImpl() throws IOException
    {
        super();
        client = new HttpClient();
        client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);
        Credentials credentials = new UsernamePasswordCredentials("tests", "secret");
        client.getState().setCredentials(new AuthScope(null, 8080, null), credentials);        
    }

    /* (non-Javadoc)
     * @see org.jboss.testharness.integration.tomcat.TomcatConnector#deploy(java.io.InputStream, java.lang.String)
     */
    @Override
    public boolean deploy(InputStream stream, String name) throws IOException
    {
        boolean result = super.deploy(stream, name);
        if(result)
        {
            GetMethod method = null;
            try
            {
                method = new GetMethod("http://localhost:8080/manager/list");
                int r = client.executeMethod(method);
                if(r == HttpURLConnection.HTTP_OK)
                {
                    String string = method.getResponseBodyAsString();
                    int start = string.indexOf(getContextName(name)+":running");
                    
                    if(start == -1)
                    {
                        this.excepton = new DeploymentException("Deployment Failure",new WebBeansConfigurationException("Deployment Failure"));
                        return false;   
                    }                    
                }
                
            }
            finally
            {
                method.releaseConnection();
            }
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see org.jboss.testharness.integration.tomcat.TomcatConnector#getDeploymentException()
     */
    @Override
    public DeploymentException getDeploymentException()
    {
        if(this.excepton != null)
        {
            return this.excepton;
        }
        
        return super.getDeploymentException();
    }

    
    
    
}
