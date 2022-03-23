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


package org.superbiz;

import java.lang.reflect.Type;

import jakarta.json.JsonObject;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;

import org.superbiz.model.User;

public class UserDeserializer implements JsonbDeserializer<User> {

	@Override
	public User deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
		JsonObject jo = parser.getObject();
		String name = jo.get("name").toString().replace("\"", "");
		if (jo.get("extra") != null) {
			name = name + jo.get("extra").toString().replace("\"", "");
		}
		User u = new User(Integer.parseInt(jo.get("id").toString()), name, null);

		return u;
	}

}
