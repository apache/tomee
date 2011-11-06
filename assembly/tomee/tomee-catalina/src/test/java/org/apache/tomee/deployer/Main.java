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
package org.apache.tomee.deployer;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.client.RemoteInitialContextFactory;

public class Main {

	public static void main(String[] args) {
		try {
			Properties p = new Properties();
			p.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
			p.setProperty(Context.PROVIDER_URL, "http://localhost:8080/openejb/ejb");
			
			InitialContext ic = new InitialContext(p);
			Deployer deployer = (Deployer) ic.lookup("openejb/WebappDeployerRemote");
			for (String arg : args) {
				AppInfo appInfo = deployer.deploy(arg);
				System.out.println(appInfo.path);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
