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

package org.apache.openejb.config.typed.util;

import org.apache.openejb.config.provider.ProviderManager;
import org.apache.openejb.config.provider.ServiceJarXmlLoader;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.Strings;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;

/**
 * @version $Rev$ $Date$
 */
public class ProviderGenerator extends Resource {

    public static void main(final String[] args) throws Exception {
        final ProviderManager manager = new ProviderManager(new ServiceJarXmlLoader());

        final Set<String> seen = new HashSet<>();

        final List<ServiceProvider> providers = manager.load("org.apache.tomee");
        for (final ServiceProvider provider : providers) {
            final List<String> types = provider.getTypes();
            final String name = guessBuilder(types);
            final String builder = name + "Builder";

            if (seen.contains(builder)) {
                continue;
            }
            seen.add(builder);

            final String service = provider.getService();

            final File file = new File("/Users/dblevins/work/all/trunk/openejb/container/openejb-core/src/main/java/org/apache/openejb/config/typed/" + builder + ".java");

            final OutputStream write = IO.write(file);
            final PrintStream out = new PrintStream(write);

            out.println("/*\n" +
                " * Licensed to the Apache Software Foundation (ASF) under one or more\n" +
                " * contributor license agreements.  See the NOTICE file distributed with\n" +
                " * this work for additional information regarding copyright ownership.\n" +
                " * The ASF licenses this file to You under the Apache License, Version 2.0\n" +
                " * (the \"License\"); you may not use this file except in compliance with\n" +
                " * the License.  You may obtain a copy of the License at\n" +
                " *\n" +
                " *     http://www.apache.org/licenses/LICENSE-2.0\n" +
                " *\n" +
                " *  Unless required by applicable law or agreed to in writing, software\n" +
                " *  distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                " *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                " *  See the License for the specific language governing permissions and\n" +
                " *  limitations under the License.\n" +
                " */");
            out.println("package org.apache.openejb.config.typed;");
            out.println();
            out.println("import org.apache.openejb.config.typed.util.*;");
            out.println("import org.apache.openejb.config.sys.*;");
            out.println("import jakarta.xml.bind.annotation.*;");
            out.println("import " + Duration.class.getName() + ";");
            out.println("import java.util.*;");
            out.println("import java.util.concurrent.*;");
            out.println("import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;");
            out.println();

            out.println(template(
                    "@XmlAccessorType(XmlAccessType.FIELD)\n" +
                        "@XmlRootElement(name = \"${name}\")\n" +
                        "public class ${builder} extends ${service} {\n"
                )
                    .apply(
                        "builder", builder,
                        "service", service,
                        "name", name
                    )
            );


            // Fields

            for (final Map.Entry<Object, Object> entry : provider.getProperties().entrySet()) {
                final String key = Strings.lcfirst(entry.getKey().toString());
                final String value = entry.getValue().toString();
                final String type = guessType(key, value);

                if (Duration.class.getName().endsWith(type)) {
                    out.println("    @XmlJavaTypeAdapter(DurationAdapter.class)");
                }
                out.println(
                    template(
                        "    @XmlAttribute\n" +
                            "    private ${type} ${key} = ${value};"
                    ).apply(
                        "builder", builder,
                        "key", key,
                        "value", asValue(type, value),
                        "type", type
                    )
                );

            }

            out.println();

            // Constructor

            out.println(template(
                    "    public ${builder}() {\n" +
                        "        setClassName(\"${className}\");\n" +
                        "        setType(\"${type}\");\n" +
                        "        setId(\"${name}\");\n"
                )
                    .apply(
                        "builder", builder,
                        "className", String.valueOf(provider.getClassName()),
                        "type", types.get(0),
                        "name", name
                    )
            );

            if (provider.getConstructor() != null) {
                out.println(template(
                    "        setConstructor(\"${constructor}\");\n")
                    .apply(
                        "constructor", fixConstructor(provider)
                    ));
            }


            if (provider.getFactoryName() != null) {
                out.println(template(
                    "        setFactoryName(\"${factoryName}\");\n")
                    .apply(
                        "factoryName", provider.getFactoryName()
                    ));
            }

            out.println("    }\n");

            // Setters

            out.println(template(
                    "    public ${builder} id(String id) {\n" +
                        "        setId(id);\n" +
                        "        return this;\n" +
                        "    }\n").apply("builder", builder)
            );

            for (final Map.Entry<Object, Object> entry : provider.getProperties().entrySet()) {
                final String lcFirstKey = Strings.lcfirst(entry.getKey().toString());
                final String ucFirstKey = Strings.ucfirst(lcFirstKey);
                final String value = entry.getValue().toString();

                final String type = guessType(lcFirstKey, value);

                // builder method

                out.println(template(
                        "    public ${builder} with${Key}(${type} ${key}) {\n" +
                            "        this.${key} = ${key};\n" +
                            "        return this;\n" +
                            "    }\n")
                        .apply(
                            "builder", builder,
                            "key", lcFirstKey,
                            "Key", ucFirstKey,
                            "value", value,
                            "type", type
                        )
                );


                // setter
                out.println(template(
                        "    public void set${Key}(${type} ${key}) {\n" +
                            "        this.${key} = ${key};\n" +
                            "    }\n")
                        .apply(
                            "key", lcFirstKey,
                            "Key", ucFirstKey,
                            "value", value,
                            "type", type
                        )
                );

                // getter
                out.println(template(
                        "    public ${type} get${Key}() {\n" +
                            "        return ${key};\n" +
                            "    }\n")
                        .apply(
                            "key", lcFirstKey,
                            "Key", ucFirstKey,
                            "value", value,
                            "type", type
                        )
                );

                if (Duration.class.getName().equals(type)) {
                    out.println(template(
                            "    public ${builder} with${Key}(long time, TimeUnit unit) {\n" +
                                "        return with${Key}(new Duration(time, unit));\n" +
                                "    }\n")
                            .apply(
                                "builder", builder,
                                "key", lcFirstKey,
                                "Key", ucFirstKey,
                                "value", value,
                                "type", type
                            )
                    );

                    out.println(template(
                            "    public void set${Key}(long time, TimeUnit unit) {\n" +
                                "        set${Key}(new Duration(time, unit));\n" +
                                "    }\n")
                            .apply(
                                "key", lcFirstKey,
                                "Key", ucFirstKey,
                                "value", value,
                                "type", type
                            )
                    );
                }

                final String s = lcFirstKey.toLowerCase(Locale.ENGLISH);
                if ("long".equals(type) && s.contains("time")) {
                    TimeUnit unit = null;
                    if (s.endsWith("millis")) {
                        unit = TimeUnit.MILLISECONDS;
                    } else if (s.endsWith("milliseconds")) {
                        unit = TimeUnit.MILLISECONDS;
                    } else if (s.endsWith("seconds")) {
                        unit = TimeUnit.SECONDS;
                    } else if (s.endsWith("minutes")) {
                        unit = TimeUnit.MINUTES;
                    } else if (s.endsWith("hours")) {
                        unit = TimeUnit.HOURS;
                    }

                    if (unit == null) {
                        continue;
                    }

                    final Pattern pattern = Pattern.compile("(millis(econds)?|seconds|minutes|hours)", Pattern.CASE_INSENSITIVE);
                    final String lcFirstKey2 = pattern.matcher(lcFirstKey).replaceAll("");
                    final String ucFirstKey2 = pattern.matcher(ucFirstKey).replaceAll("");

                    out.println(template(
                            "    public ${builder} with${Key2}(long time, TimeUnit unit) {\n" +
                                "        return with${Key}(TimeUnit.${unit}.convert(time, unit));\n" +
                                "    }\n")
                            .apply(
                                "builder", builder,
                                "key2", lcFirstKey2,
                                "Key2", ucFirstKey2,
                                "key", lcFirstKey,
                                "Key", ucFirstKey,
                                "value", value,
                                "unit", unit.name(),
                                "type", type
                            )
                    );

                    out.println(template(
                            "    public void set${Key2}(long time, TimeUnit unit) {\n" +
                                "        set${Key}(TimeUnit.${unit}.convert(time, unit));\n" +
                                "    }\n")
                            .apply(
                                "key2", lcFirstKey2,
                                "Key2", ucFirstKey2,
                                "key", lcFirstKey,
                                "Key", ucFirstKey,
                                "value", value,
                                "unit", unit.name(),
                                "type", type
                            )
                    );
                }
            }

            out.println(
                "    public Properties getProperties() {\n" +
                    "        return Builders.getProperties(this);\n" +
                    "    }\n"
            );


            out.println("}");
            out.flush();
            out.close();
        }
    }

    private static String fixConstructor(final ServiceProvider provider) {
        final String s = String.valueOf(provider.getConstructor());
        final String[] split = s.split(" *, *");
        for (int i = 0; i < split.length; i++) {
            split[i] = Strings.lcfirst(split[i]);
        }
        return Join.join(", ", split);
    }

    private static String asValue(final String type, final String value) {
        if ("".equals(value)) {
            return "null";
        }

        if ("String".equals(type)) {
            return "\"" + value + "\"";
        }
        if (URI.class.getName().equals(type)) {
            return URI.class.getName() + ".create(\"" + value + "\")";
        }
        if (Duration.class.getName().equals(type)) {
            return Duration.class.getName() + ".parse(\"" + value + "\")";
        }

        return value;
    }

    private static String guessBuilder(final List<String> types) {
        String s = types.get(0);

        if ("STATEFUL".equals(s)) {
            return "StatefulContainer";
        }
        if ("SINGLETON".equals(s)) {
            return "SingletonContainer";
        }
        if ("MANAGED".equals(s)) {
            return "ManagedContainer";
        }
        if ("STATELESS".equals(s)) {
            return "StatelessContainer";
        }
        if ("MESSAGE".equals(s)) {
            return "MessageDrivenContainer";
        }
        if ("BMP_ENTITY".equals(s)) {
            return "BmpEntityContainer";
        }
        if ("CMP_ENTITY".equals(s)) {
            return "CmpEntityContainer";
        }

        if ("jakarta.jms.ConnectionFactory".equals(s)) {
            return "JmsConnectionFactory";
        }
        if ("jakarta.mail.Session".equals(s)) {
            return "JavaMailSession";
        }

        s = s.replaceAll(".*\\.", "");
        return s;
    }

    private static String guessType(final String key, final String value) {
        if (value.equals("true") || value.equals("false")) {
            return "boolean";
        }

        if (key.toLowerCase(Locale.ENGLISH).endsWith("timeout")) {
            return Duration.class.getName();
        }

        if (value.matches("-?[0-9]+ +(m|h|s|M|H|S)[^ ]*")) {
            return Duration.class.getName();
        }


        if (key.toLowerCase(Locale.ENGLISH).contains("time")) {
            try {
                Long.parseLong(value);
                return "long";
            } catch (final NumberFormatException e) {
                // no-op
            }
        }

        try {
            Integer.parseInt(value);
            return "int";
        } catch (final NumberFormatException e) {
            // no-op
        }

        if (key.toLowerCase(Locale.ENGLISH).endsWith("url")) {
            return URI.class.getName();
        }

        if (key.toLowerCase(Locale.ENGLISH).endsWith("uri")) {
            return URI.class.getName();
        }

        return "String";
    }

    public static Template template(final String template) {
        return new Template(template);
    }

    public static class Template {

        public static final Pattern PATTERN = Pattern.compile("(\\$\\{)((\\.|\\w)+)(})");
        private final String template;

        public Template(final String template) {
            this.template = template;
        }


        public String apply(final String... args) {
            final Map<String, String> map = new HashMap<>();

            for (int i = 0; i < args.length; i += 2) {
                final String key = args[i];
                final String value = args[i + 1];
                map.put(key, value);
            }

            return apply(map);
        }

        public String apply(final Map<String, String> map) {
            final Matcher matcher = PATTERN.matcher(template);
            final StringBuffer buf = new StringBuffer();

            while (matcher.find()) {
                final String key = matcher.group(2);

                if (key == null) {
                    throw new IllegalStateException("Key is null. Template '" + template + "'");
                }

                String value = map.get(key);

                if (key.toLowerCase(Locale.ENGLISH).endsWith(".lc")) {
                    value = map.get(key.substring(0, key.length() - 3)).toLowerCase(Locale.ENGLISH);
                } else if (key.toLowerCase(Locale.ENGLISH).endsWith(".uc")) {
                    value = map.get(key.substring(0, key.length() - 3)).toUpperCase(Locale.ENGLISH);
                } else if (key.toLowerCase(Locale.ENGLISH).endsWith(".cc")) {
                    value = Strings.camelCase(map.get(key.substring(0, key.length() - 3)));
                }

                if (value == null) {
                    throw new IllegalStateException("Value is null for key '" + key + "'. Template '" + template + "'. Keys: " + Join.join(", ", map.keySet()));
                }
                matcher.appendReplacement(buf, value);
            }

            matcher.appendTail(buf);
            return buf.toString();
        }

    }

}
