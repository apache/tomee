/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.ejblookup;

import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

//START SNIPPET: code
public class BlueBean implements Friend {

    public String sayHello() {
        return "Blue says, Hello!";
    }

    public String helloFromFriend() {
        try {
            Friend friend = (Friend) new InitialContext().lookup("java:comp/env/myFriend");
            return "My friend " + friend.sayHello();
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
}
//END SNIPPET: code
