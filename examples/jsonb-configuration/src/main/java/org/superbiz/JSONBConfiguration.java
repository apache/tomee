package org.superbiz;

import java.util.Locale;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyNamingStrategy;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class JSONBConfiguration implements ContextResolver<Jsonb> {

	private Jsonb jsonb;

	public JSONBConfiguration() {
		// jsonbConfig offers a lot of configurations.
		JsonbConfig config = new JsonbConfig().withFormatting(true)
				.withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
				.withDateFormat("yyyy - MM - dd", Locale.ENGLISH);

		jsonb = JsonbBuilder.create(config);
	}

	@Override
	public Jsonb getContext(Class<?> type) {
		return jsonb;
	}

}
