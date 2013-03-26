//
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

name := "scala-basic"

version := "1.1.0"

scalaVersion := "2.9.1"

resolvers ++= Seq(
	"Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
	"Apache Snapshots" at "https://repository.apache.org/content/groups/snapshots/"
)

libraryDependencies ++= Seq(
    "org.apache.openejb" % "javaee-api" % "6.0-5",
    "org.apache.openejb" % "openejb-core" % "4.6.0.beta1" % "test",
    "org.scalatest" %% "scalatest" % "1.6.1" % "test"
)

