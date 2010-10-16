/**
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
package org.apache.openejb.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
* @version $Rev$ $Date$
*/
public class CommandParser {

    private final Map<String, Option> opts = new LinkedHashMap<String, Option>();

    public CommandParser() {
        init();
    }

    protected void init(){}

    protected List<String> validate(Arguments arguments) {
        return new ArrayList<String>();
    }

    protected List<String> usage() {
        return new ArrayList<String>();
    }

    public void category(String name) {
        opts.put("-@" + name, new Category(name));
    }

    public Option opt(String _long) {
        return opt(new Option(_long));
    }

    public Option opt(char _short, String _long) {
        return opt(new Option(_short, _long));
    }

    private Option opt(Option o) {
        opts.put(o.getName(), o);
        if (o.getMini() != null) opts.put(o.getMini(), o);
        return o;
    }

    private void help() {

        final Iterator<String> usage = usage().iterator();

        if (usage.hasNext()) {
            System.out.println("Usage: " + usage.next());
        }

        while (usage.hasNext()) {
            System.out.println("  or   " + usage.next());
        }

        List<Option> seen = new ArrayList<Option>();

        for (Option option : opts.values()) {
            if (seen.contains(option)) continue;
            seen.add(option);

            if (option instanceof Category) {
                System.out.println("");
                System.out.println(option.getName() + ":");
                continue;
            }

            StringBuilder s = new StringBuilder();
            if (option.getMini() != null) {
                s.append(" -");
                s.append(option.getMini());
                s.append(",  ");
            } else {
                s.append("      ");
            }

            s.append("--");
            s.append(option.getName());
            if (option.getType() != null) {
                s.append("=");
                s.append(option.getType().getSimpleName());
            }

            if (option.getDescription() != null || option.getValue() != null) {
                for (int i = s.length(); i < 29; i++) {
                    s.append(' ');
                }

                s.append(' ');
                s.append(' ');

                if (option.getDescription() != null) {
                    s.append(option.getDescription());
                }

                if (option.getValue() != null) {
                    if (option.getDescription() != null) s.append(" ");
                    s.append("Default is ");
                    s.append(option.getValue());
                }
            }

            System.out.println(s.toString());
        }
    }

    public Arguments parse(String[] args) throws HelpException, InvalidOptionsException {
        List<String> list = new ArrayList<String>(Arrays.asList(args));

        ListIterator<String> items = list.listIterator();

        Properties props = new Properties();

        while (items.hasNext()) {
            String arg = items.next();

            if (!arg.startsWith("-")) break;

            items.remove();

            boolean longOption = arg.startsWith("--");

            arg = arg.replaceFirst("-+", "");

            if (longOption) {
                String name, value = "";

                if (arg.equals("help")) {
                    props.put("help", "");
                    continue;
                } else if (arg.indexOf("=") > -1) {
                    name = arg.substring(0, arg.indexOf("="));
                    value = arg.substring(arg.indexOf("=") + 1);
                } else {
                    name = arg;

                    final Option option = opts.get(name);

                    value = value(items, option);
                }

                Option option = opts.get(name);

                checkOption("--" + name, option);
                checkValue("--" + name, value, option);
                props.put(option.getName(), value);
            } else {
                char[] chars = new char[arg.length()];
                arg.getChars(0, chars.length, chars, 0);

                for (int i = 0; i < chars.length; i++) {
                    char c = chars[i];

                    String name = c + "";

                    Option option = opts.get(name);

                    checkOption("-" + name, option);

                    String value = "";

                    if (i + 1 >= chars.length) {
                        // Last char so, next item could be a value
                        value = value(items, option);
                    }

                    checkValue("-" + c, value, option);
                    props.put(option.getName(), value);
                }
            }
        }

        final Arguments arguments = new Arguments(new Options(props), list);

        if (arguments.options().has("help")) {

            help();

            throw new HelpException();
        }

        final List<String> issues = validate(arguments);

        if (issues.size() > 0) {

            for (String issue : issues) {
                System.err.println(issue);
            }

            System.err.println();
            
            help();

            throw new InvalidOptionsException();
        }
        
        return arguments;
    }

    private void checkValue(String optString, String value, Option option) {
        checkOption(optString, option);

        if (option.getType() == null) return;

        if (value == null || value.equals("")) {
            System.err.println("Missing param " + optString + " <" + option.getType().getSimpleName() + ">");
            System.exit(1);
        }
    }

    private void checkOption(String invalid, Option option) {
        if (option != null) return;

        System.err.println("Unknown option: " + invalid);

        help();

        System.exit(1);
    }

    private String value(ListIterator<String> items, Option option) {
        if (option.getType() != null && items.hasNext()) {

            String possibleValue = items.next();

            if (!possibleValue.startsWith("-")) {
                items.remove();
                // next item is the value
                return possibleValue;
            } else {
                // next item is an option
                items.previous();
            }
        }
        return "";
    }

    public static final class Arguments {
        private final Options options;
        private final List<String> args;

        public Arguments(Options options, List<String> args) {
            this.options = options;
            this.args = args;
        }

        public Options options() {
            return options;
        }

        public List<String> get() {
            return args;
        }
    }

    public static class Category extends Option {
        public Category(String name) {
            super(name);
        }
    }

    public static class Option {
        private final String mini;
        private final String name;
        private String description;
        private Class type;
        private Object value;

        Option(char mini, String name) {
            this(mini + "", name);
        }

        Option(String name) {
            this(null, name);
        }

        Option(String mini, String name) {
            this.mini = mini;
            this.name = name;
        }

        public Option description(String description) {
            if (!description.endsWith(".")) description += ".";
            this.setDescription(description);
            return this;
        }

        public Option type(Class type) {
            this.setType(type);
            return this;
        }

        public Option value(Object value) {
            this.setValue(value);
            if (getType() == null) this.setType(value.getClass());
            return this;
        }

        public String getMini() {
            return mini;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Class getType() {
            return type;
        }

        public void setType(Class type) {
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    
    public static class HelpException extends Exception {

    }

    public static class InvalidOptionsException extends Exception {
        
    }
}
