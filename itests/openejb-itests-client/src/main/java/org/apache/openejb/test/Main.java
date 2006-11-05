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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test;

public class Main {

    public static void main(String args[]) {
//        try{
//            File directory = SystemInstance.get().getHome().getDirectory("lib");
//            SystemInstance.get().getClassPath().addJarsToPath(directory);
//            File directory1 = SystemInstance.get().getHome().getDirectory("dist");
//            SystemInstance.get().getClassPath().addJarsToPath(directory1);
//        } catch (Exception e){
//            e.printStackTrace();
//        }

        TestRunner.main(args);
    }

}
