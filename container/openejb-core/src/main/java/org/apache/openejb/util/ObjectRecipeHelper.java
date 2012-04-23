package org.apache.openejb.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.openejb.loader.SystemInstance;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

public final class ObjectRecipeHelper {
    private ObjectRecipeHelper() {
        // no-op
    }

    public static Object createMeFromSystemProps(final String prefix, final String suffix, final Class<?> clazz) {
        final Properties props = SystemInstance.get().getProperties();
        final Map<String, Object> usedOnes = new HashMap<String, Object>();

        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            final String key = entry.getKey().toString();
            if (prefix != null && !key.startsWith(prefix)) {
                continue;
            }
            if (suffix != null && !key.endsWith(suffix)) {
                continue;
            }

            String newKey = key;
            if (prefix != null) {
                newKey = newKey.substring(prefix.length());
            }
            if (suffix != null) {
                newKey = newKey.substring(0, newKey.length() - suffix.length());
            }
            usedOnes.put(newKey, entry.getValue());
        }

        final ObjectRecipe recipe = new ObjectRecipe(clazz);
        recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        recipe.allow(Option.PRIVATE_PROPERTIES);
        recipe.allow(Option.FIELD_INJECTION);
        recipe.allow(Option.NAMED_PARAMETERS);
        recipe.setAllProperties(usedOnes);
        return recipe.create();
    }
}
