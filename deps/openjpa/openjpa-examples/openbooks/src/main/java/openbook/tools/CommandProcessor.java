/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package openbook.tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Processes options.
 * <br>
 * User can register a set of command options. Then this processor will parse a set of Strings to
 * store the values for each of the registered options as well as optionally any unrecognized
 * option values.
 * 
 * @author Pinaki Poddar
 *
 */
public class CommandProcessor {
    private final Map<Option, String> registeredOptions = new HashMap<Option,String>();
    private final Set<Option> unregisteredOptions = new HashSet<Option>();
    private boolean allowsUnregisteredOption = true;
    
    /**
     * Set the option values from the given arguments.
     * All elements of the given array is <em>not</em> consumed,
     * only till the index that appears to be a valid option.
     * 
     * @see #lastIndex(String[])
     * 
     * @param args an array of arguments.
     * 
     * @return the array elements that are not consumed.
     */
    public String[] setFrom(String[] args) {
        return setFrom(args, 0, args != null ? lastIndex(args) : 0);
    }
    
    /**
     * Set the option values from the given arguments between the given indices.
     * 
     * @see #lastIndex(String[])
     * 
     * @param args an array of arguments.
     * 
     * @return the array elements that are not consumed.
     */
    public String[] setFrom(String[] args, int from, int to) {
        if (args == null) 
            return null;
        if (args.length == 0)
            return new String[0];
        assertValidIndex(from, args, "Initial index " + from + " is an invalid index to " + Arrays.toString(args));
        assertValidIndex(to, args, "Last index " + to + " is an invalid index to " + Arrays.toString(args));
            
        int i = from;
        for (; i < to; i++) {
            String c = args[i];
            Option command = findCommand(c);
            if (command == null) {
                throw new IllegalArgumentException(c + " is not a recongined option");
            }
            if (command.requiresInput()) {
                i++;
            }
            if (i > to) {
                throw new IllegalArgumentException("Command " + c + " requires a value, but no value is specified");
            }
            registeredOptions.put(command, args[i]);
        }
        String[] remaining = new String[args.length-to];
        System.arraycopy(args, i, remaining, 0, remaining.length);
        return remaining;
    }
    
    /**
     * Gets the last index in the given array that can be processed as an option.
     * The array elements are sequentially tested if they are a valid option name 
     * (i.e. starts with - character) and if valid then the next element is consumed
     * as value, if the option requires a value. The search ends when either 
     * the array is exhausted or encounters elements that are not options. 
     * 
     * @param args an array of arguments
     * @return the last index that will/can be consumed by this processor.
     */
    public int lastIndex(String[] args) {
        int i = 0;
        for (; i < args.length; i++) {
            if (Option.isValidName(args[i])) {
                Option cmd = findCommand(args[i]);
                if (cmd != null) {
                    if (cmd.requiresInput()) {
                        i++;
                    }
                    continue;
                }
            }
            break;
        }
        return i;
    }
    
    /**
     * Register the given aliases as a command option.
     * 
     * @param requiresValue if true then the option must be specified with a value.
     * @param aliases strings to recognize this option. Each must begin with a dash character.
     * 
     * @return the command that is registered
     */
    public Option register(boolean requiresValue, String...aliases) {
        Option option = new Option(requiresValue, aliases);
        registeredOptions.put(option, null);
        return option;
    }
    
    /**
     * Finds a command with the given name.
     * If no command has been registered with the given name, but this processor
     * allows unrecognized options, then as a result of this call, the
     * unknown name is registered as an option.
     *  
     * @param option a command alias.
     * 
     * @return null if the given String is not a valid command option name.
     * 
     */
    public Option findCommand(String option) {
        if (!Option.isValidName(option))
            return null;
        for (Option registeredOption : registeredOptions.keySet()) {
            if (registeredOption.match(option))
                return registeredOption;
        }
        for (Option unregisteredOption : unregisteredOptions) {
            if (unregisteredOption.match(option))
                return unregisteredOption;
        }
        if (allowsUnregisteredOption) {
            Option cmd = new Option(option);
            unregisteredOptions.add(cmd);
            return cmd;
        } else {
            return null;
        }
    }
    
    /**
     * Gets all the unrecognized command options.
     * 
     * @return empty set if no commands are unrecognized.
     */
    public Set<Option> getUnregisteredCommands() {
        return Collections.unmodifiableSet(unregisteredOptions);
    }
    
    <T> void assertValidIndex(int i, T[] a, String message) {
        if  (i <0 || (a != null && i >= a.length))
            throw new ArrayIndexOutOfBoundsException(message);
    }
    
    /**
     * Gets value of the option matching the given alias.
     *   
     * @param alias an alias.
     * 
     * @return value of the given option.
     */
    public String getValue(String alias) {
        Option cmd = findCommand(alias);
        return getValue(cmd);
    }
    
    /**
     * Gets value of the given option.
     * 
     * @param opt an option.
     * 
     * @return value of the given option.
     */
    String getValue(Option opt) {
        String val = registeredOptions.get(opt);
        if (val == null)
            val = opt.getDefaultValue();
        return val;
    }
    
    /**
     * @return the allowsUnregisteredOption
     */
    public boolean getAllowsUnregisteredOption() {
        return allowsUnregisteredOption;
    }

    /**
     * @param allowsUnregisteredOption the allowsUnregisteredOption to set
     */
    public void setAllowsUnregisteredOption(boolean allowsUnregisteredOption) {
        this.allowsUnregisteredOption = allowsUnregisteredOption;
    }
    
    /**
     * A simple immutable object represents meta-data about a command option.
     *  
     * @author Pinaki Poddar
     *
     */
    public static class Option {
        private static final String DASH = "-";
        
        /**
         * Affirms if the given string can be a valid option name.
         * An option name always starts with dash and must be followed by at least one character.
         */
        public static boolean isValidName(String s) {
            return s != null && s.startsWith(DASH) && s.length() > 1;
        }
        
        /**
         * Possible names of this command option.
         * All aliases must start with a dash (<code>-</code>). 
         */
        private String[] aliases;
        
        /**
         * Does the option require a value?
         */
        private boolean requiresInput;
        
        /**
         * A default value for this option.
         */
        private String defValue;
        
        /**
         * A description String.
         */
        private String _description = "";
        
        /**
         * Create a command with given aliases. This option requires a value.
         *  
         * @param aliases strings each must start with a dash (<code>-</code>).  
         */
        public Option(String... aliases) {
            this(true, aliases);
        }
        
        /**
         * Create a option with given aliases.
         * 
         * @param requiresInput does it require a value?
         * @param aliases strings each must start with a dash (<code>-</code>).  
         */
        public Option(boolean requiresInput, String...aliases) {
            super();
            if (aliases == null || aliases.length == 0)
                throw new IllegalArgumentException("Can not create command with null or empty aliases");
            for (String alias : aliases) {
                if (!isValidName(alias)) {
                    throw new IllegalArgumentException("Invalid alias [" + alias + "]. " +
                            "Aliases must start with - followded by at least one character");
                }
            }
            this.aliases = aliases;
            this.requiresInput = requiresInput;
        }
        
        /**
         * Gets the first alias as the name.
         */
        public String getName() {
            return aliases[0];
        }
        
        /**
         * Sets the default value for this option.
         * 
         * @param v a default value.
         * 
         * @return this command itself.
         * 
         * @exception IllegalStateException if this option does not require a value.
         */
        public Option setDefault(String v) {
            if (!requiresInput)
                throw new IllegalStateException(this +
                    " does not require a value. Can not set default value [" +
                    v + "]");
            defValue = v;
            return this;
        }
        
        public Option setDescription(String desc) {
            if (desc != null) {
                _description = desc;
            }
            return this;
        }
        
        public String getDescription() {
            return _description;
        }
        
        /**
         * Affirms if the given name any of the aliases.
         * @param alias
         * @return
         */
        public boolean match(String name) {
            for (String alias : aliases) {
                if (name.equals(alias))
                    return true;
            }
            return false;
        }
        
        /**
         * Affirms if this option requires a value.
         */
        public boolean requiresInput() {
            return requiresInput;
        }
        
        /**
         * Gets the default value of this option.
         * 
         * @return the default value. null if no default value has been set.
         */
        public String getDefaultValue() {
            return defValue;
        }
        
        public String toString() {
            return getName();
        }
    }


}
