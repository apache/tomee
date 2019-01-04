package org.superbiz;

import java.lang.reflect.Type;

import javax.json.JsonObject;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;

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
