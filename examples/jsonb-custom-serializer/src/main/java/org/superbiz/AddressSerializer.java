package org.superbiz;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

import org.superbiz.model.Address;

public class AddressSerializer implements JsonbSerializer<Address> {

	@Override
	public void serialize(Address obj, JsonGenerator generator, SerializationContext ctx) {
		if (obj != null) {
			obj.setAddr("modified - " + obj.getAddr());
			ctx.serialize(obj, generator);
		}

	}

}
