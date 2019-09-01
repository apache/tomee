package org.superbiz;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class JSONBConfiguration implements ContextResolver<Jsonb> {

	private Jsonb jsonb;

	public JSONBConfiguration() {
		JsonbConfig config = new JsonbConfig().withFormatting(true).withSerializers(new AddressSerializer());

		jsonb = JsonbBuilder.create(config);
	}

	@Override
	public Jsonb getContext(Class<?> type) {
		return jsonb;
	}

}
