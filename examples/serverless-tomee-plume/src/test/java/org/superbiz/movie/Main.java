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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.movie;

import org.apache.tomee.bootstrap.Server;

public class Main {
    public static void main(String[] args) {
        final Server server = Server.builder().build();

        System.out.println("Listening for requests at " + server.getURI());
    }


//    public static void main(String[] args) {
//        Server.builder()
//                .home(file -> Stream.of(file.listFiles()).forEach(System.out::println))
//                .build();
//    }
//
}
