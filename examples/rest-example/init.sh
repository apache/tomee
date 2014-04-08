#! /bin/bash
#
#   Licensed to the Apache Software Foundation (ASF) under one or more
#   contributor license agreements.  See the NOTICE file distributed with
#   this work for additional information regarding copyright ownership.
#   The ASF licenses this file to You under the Apache License, Version 2.0
#   (the "License"); you may not use this file except in compliance with
#   the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
base=http://localhost:8080/rest-example

for i in {1..3}; do
	curl -i -H "Accept: application/json" -X PUT "$base/api/user/create?name=Wizard$i&pwd=simplest&&mail=supername$i@supercompany.com"
done

for i in {1..30}; do
	curl -i -H "Accept: application/json" -X PUT "$base/api/post/create?title=Title$i&content=Content&userId=$((RANDOM % 3 + 1 ))"
done

for i in {1..1000}; do
	curl -i -H "Accept: application/json" -X PUT "$base/api/comment/create?author=Author$i&content=Content$i&postId=$((RANDOM % 30 + 51 ))"
done

