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
package org.apache.webbeans.se.sample.beans;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import javax.inject.Singleton;

import org.apache.webbeans.se.sample.Login;
import org.apache.webbeans.se.sample.bindings.FileLoginBinding;

@FileLoginBinding
@Singleton
public class FileLogin implements Login
{
    private static Properties properties = null;

    static
    {
        properties = new Properties();
        try
        {
            properties.load(FileLogin.class.getResourceAsStream("/login.properties"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public void logout()
    {
        
    }

    @Override
    public boolean login(String userName, char[] password)
    {
        Object value = properties.get(userName);
        if(value == null)
        {
            return false;
        }
        
        char[] pass = value.toString().toCharArray();
        
        if(Arrays.equals(pass, password))
        {
            Arrays.fill(pass, '0');
            return true;
        }
        
        return false;
    }

}
