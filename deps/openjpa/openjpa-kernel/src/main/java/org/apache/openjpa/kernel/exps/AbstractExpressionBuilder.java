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
package org.apache.openjpa.kernel.exps;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.StringDistance;
import org.apache.openjpa.lib.util.Localizer.Message;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.XMLMetaData;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.UnsupportedException;
import org.apache.openjpa.util.UserException;
import serp.util.Strings;

/**
 * Abstract base class to help build expressions. Provides
 * generic language-independent support for variable resolution,
 * path traversal, and error messages.
 *
 * @author Marc Prud'hommeaux
 * @nojavadoc
 */
public abstract class AbstractExpressionBuilder {

    // used for error messages
    protected static final int EX_USER = 0;
    protected static final int EX_FATAL = 1;
    protected static final int EX_UNSUPPORTED = 2;

    // common implicit type settings
    public static final Class<Object> TYPE_OBJECT = Object.class;
    public static final Class<String> TYPE_STRING = String.class;
    public static final Class<Character> TYPE_CHAR_OBJ = Character.class;
    public static final Class<Number> TYPE_NUMBER = Number.class;
    public static final Class<Collection> TYPE_COLLECTION = Collection.class;
    public static final Class<Map> TYPE_MAP = Map.class;

    // contains types for setImplicitTypes
    public static final int CONTAINS_TYPE_ELEMENT = 1;
    public static final int CONTAINS_TYPE_KEY = 2;
    public static final int CONTAINS_TYPE_VALUE = 3;

    private static final Localizer _loc = Localizer.forPackage
        (AbstractExpressionBuilder.class);

    protected final Resolver resolver;
    protected ExpressionFactory factory;

    private final Set<ClassMetaData> _accessPath = new HashSet<ClassMetaData>();
    private Map<String,Value> _seenVars = null;
    private Set<Value> _boundVars = null;

    /**
     * Constructor.
     *
     * @param factory the expression factory to use
     * @param resolver used to resolve variables, parameters, and class
     * names used in the query
     */
    public AbstractExpressionBuilder(ExpressionFactory factory,
        Resolver resolver) {
        this.factory = factory;
        this.resolver = resolver;
    }

    /**
     * Returns the class loader that should be used for resolving
     * class names (in addition to the resolver in the query).
     */
    protected abstract ClassLoader getClassLoader();

    /**
     * Create a proper parse exception for the given reason.
     */
    protected OpenJPAException parseException(int e, String token,
        Object[] args,
        Exception nest) {
        String argStr;
        if (args == null)
            argStr = getLocalizer().get(token).getMessage();
        else
            argStr = getLocalizer().get(token, args).getMessage();

        Message msg = _loc.get("parse-error", argStr, currentQuery());

        switch (e) {
            case EX_FATAL:
                throw new InternalException(msg, nest);
            case EX_UNSUPPORTED:
                throw new UnsupportedException(msg, nest);
            default:
                throw new UserException(msg, nest);
        }
    }

    /**
     * Register the specified metadata as being in the query's access path.
     */
    protected ClassMetaData addAccessPath(ClassMetaData meta) {
        _accessPath.add(meta);
        return meta;
    }

    /**
     * Return the recorded query access path.
     */
    protected ClassMetaData[] getAccessPath() {
        return (ClassMetaData[]) _accessPath.toArray
            (new ClassMetaData[_accessPath.size()]);
    }

    /**
     * Return true if the given variable has been bound.
     */
    protected boolean isBound(Value var) {
        return _boundVars != null && _boundVars.contains(var);
    }

    /**
     * Record that the given variable is bound.
     */
    protected void bind(Value var) {
        if (_boundVars == null)
            _boundVars = new HashSet<Value>();
        _boundVars.add(var);
    }

    /**
     * Returns a value for the given id.
     */
    protected Value getVariable(String id, boolean bind) {
        // check for already constructed var
        if (isSeenVariable(id))
            return getVariable(id);

        return createVariable(id, bind);
    }

    protected Value createVariable(String id, boolean bind) {
        // create and cache var
        Class<?> type = getDeclaredVariableType(id);

        // add this type to the set of classes in the filter's access path
        ClassMetaData meta = null;
        if (type == null)
            type = TYPE_OBJECT;
        else
            meta = getMetaData(type, false);
        if (meta != null) {
            addAccessPath(meta);
            addSchemaToContext(id, meta);
        }

        Value var = null;
        if (bind)
            var = factory.newBoundVariable(id, type);
        else
            var = factory.newUnboundVariable(id, type);
        var.setMetaData(meta);

        if (_seenVars == null)
            _seenVars = new HashMap<String,Value>();
        _seenVars.put(id, var);

        addVariableToContext(id, var);
        return var;
    }

    /**
     * Validate that all unbound variables are of a PC type. If not, assume
     * that the user actually made a typo that we took for an implicit
     * unbound variable.
     */
    protected void assertUnboundVariablesValid() {
        if (_seenVars == null)
            return;

        Value var;
        for (Map.Entry<String,Value> entry : _seenVars.entrySet()) {
            var = entry.getValue();
            if (var.getMetaData() == null && !isBound(var)
                && !isDeclaredVariable(entry.getKey())) {
                throw parseException(EX_USER, "not-unbound-var",
                    new Object[]{ entry.getKey() }, null);
            }
        }
    }

    /**
     * Returns whether the specified variable name has been explicitly
     * declared. Note all query languages necessarily support declaring
     * variables.
     *
     * @param id the variable to check
     * @return true if the variabe has been explicitely declared
     */
    protected abstract boolean isDeclaredVariable(String id);

    /**
     * Return whether the given id has been used as a variable.
     */
    protected boolean isSeenVariable(String id) {
        return _seenVars != null && _seenVars.containsKey(id);
    }

    /**
     * Convenience method to get metadata for the given type.
     */
    protected ClassMetaData getMetaData(Class<?> c, boolean required) {
        return getMetaData(c, required, getClassLoader());
    }

    /**
     * Convenience method to get metadata for the given type.
     */
    protected ClassMetaData getMetaData(Class<?> c, boolean required,
        ClassLoader loader) {
        return resolver.getConfiguration().getMetaDataRepositoryInstance().
            getMetaData(c, loader, required);
    }

    /**
     * Traverse the given field in the given path.
     */
    protected Value traversePath(Path path, String field) {
        return traversePath(path, field, false, false);
    }
    
    protected Value traverseXPath(Path path, String field) {
        XMLMetaData meta = path.getXmlMapping();
        if (meta.getFieldMapping(field) == null) {
            throw parseException(EX_USER, "no-field",
                    new Object[]{ meta.getType(), field }, null);
        }
        else {
            // collection-valued xpath is not allowed
            int type = meta.getFieldMapping(field).getTypeCode();
            switch (type) {
                case JavaTypes.ARRAY:
                case JavaTypes.COLLECTION:
                case JavaTypes.MAP:
                    throw new UserException(_loc.get("collection-valued-path",
                            field));
            }
        }
        path.get(meta, field);
        return path;
    }

    /**
     * Traverse the given field in the given path.
     */
    protected Value traversePath(Path path, String field, boolean pcOnly,
        boolean allowNull) {
        ClassMetaData meta = path.getMetaData();
        if (meta == null)
            throw parseException(EX_USER, "path-no-meta",
                new Object[]{ field, path.getType() }, null);

        FieldMetaData fmd = meta.getField(field);
        if (fmd == null) {
            Object val = traverseStaticField(meta.getDescribedType(), field);
            if (val == null) {
                if (isMultiValuedTraversalAttempt(path, field)) {
                    throw parseException(EX_USER, "multi-valued-travesal", new Object[]{field, path.last()}, null);
                }
            	String[] all = meta.getFieldNames();
            	Class<?> cls = meta.getDescribedType();
                throw parseException(EX_USER, "no-field",
                    new Object[] {field, cls.getSimpleName(), 
                    StringDistance.getClosestLevenshteinDistance(field, all), 
                	cls.getName(), Arrays.toString(all)}, null);
            }

            return factory.newLiteral(val, Literal.TYPE_UNKNOWN);
        }

        if (fmd.isEmbedded())
            meta = fmd.getEmbeddedMetaData();
        else
            meta = fmd.getDeclaredTypeMetaData();
        if (meta != null) {
            addAccessPath(meta);
            path.setMetaData(meta);
        }
        else {
            // xmlsupport xpath
            XMLMetaData xmlmeta = fmd.getRepository().getXMLMetaData(fmd.getDeclaredType());
            if (xmlmeta != null) {
                path.get(fmd, xmlmeta);
                return path;
            }
        }

        if (meta != null || !pcOnly) 
            path.get(fmd, allowNull);

        return path;
    }

    /**
     * Return a constant containing the value of the given static field.
     */
    protected Object traverseStaticField(Class<?> cls, String field) {
        try {
            return cls.getField(field).get(null);
        } catch (Exception e) {
            // count not locate the field: return null
            return null;
        }
    }
    
    private boolean isMultiValuedTraversalAttempt(Path path, String field) {
        if (path == null) return false;
        if (path.last() == null) return false;
        if (path.last().getElement() == null) return false;
        if (path.last().getElement().getDeclaredTypeMetaData() == null) return false;
        if (path.last().getElement().getDeclaredTypeMetaData().getField(field) == null) return false;
        return true;
    }

    /**
     * Returns the type of the named variable if it has been declared.
     */
    protected abstract Class<?> getDeclaredVariableType(String name);

    /**
     * Set the implicit types of the given values based on the fact that
     * they're used together, and based on the operator type.
     */
    protected void setImplicitTypes(Value val1, Value val2,
        Class<?> expected) {
        setImplicitTypes(val1, val2, expected, resolver);
    }
    protected static void setImplicitTypes(Value val1, Value val2,
        Class<?> expected, Resolver resolver) {
        Class<?> c1 = val1.getType();
        Class<?> c2 = val2.getType();
        boolean o1 = c1 == TYPE_OBJECT;
        boolean o2 = c2 == TYPE_OBJECT;

        if (o1 && !o2) {
            val1.setImplicitType(c2);
            if (val1.getMetaData() == null && !val1.isXPath())
                val1.setMetaData(val2.getMetaData());
        } else if (!o1 && o2) {
            val2.setImplicitType(c1);
            if (val2.getMetaData() == null && !val1.isXPath())
                val2.setMetaData(val1.getMetaData());
        } else if (o1 && o2 && expected != null) {
            // we never expect a pc type, so don't bother with metadata
            val1.setImplicitType(expected);
            val2.setImplicitType(expected);
        } else if (isNumeric(val1.getType()) != isNumeric(val2.getType())) {
            if (resolver.getConfiguration().getCompatibilityInstance().
                getQuotedNumbersInQueries())
                convertTypesQuotedNumbers(val1, val2);
            else
                convertTypes(val1, val2);
        }
//        convertLiteralToAvoidCast(val1, val2);
    }

//    private static void convertLiteralToAvoidCast(Value val1, Value val2) {
//        boolean l1 = val1 instanceof Literal;
//        boolean l2 = val2 instanceof Literal;
//        Class<?> c1 = val1.getType();
//        Class<?> c2 = val2.getType();
//        if (l1 ^ l2 && c1 != c2) {
//            // if one side is a literal and types are different
//            if (l1 && isNumeric(c1) && canDownTypeValue(((Literal)val1).getValue(), c1, c2)) {
//                val1.setImplicitType(c2);
//            } else if (l2 && isNumeric(c2) && canDownTypeValue(((Literal)val2).getValue(), c2, c1)) {
//                val2.setImplicitType(c1);
//            }
//        }
//    }
//
//    private static boolean canDownTypeValue(Object val, Class<?> fromType, Class<?> toType) {
//        long testVal = ( fromType == Character.TYPE || fromType == TYPE_CHAR_OBJ ) 
//            ? ((Character)val).charValue()
//            : ((Number)val).longValue();
//        long min;
//        long max;
//        switch (JavaTypes.getTypeCode(toType)) {
//        case JavaTypes.BYTE:
//            min = Byte.MIN_VALUE;
//            max = Byte.MAX_VALUE;
//            break;
//        case JavaTypes.CHAR:
//            min = Character.MIN_VALUE;
//            max = Character.MAX_VALUE;
//            break;
//        case JavaTypes.SHORT:
//            min = Short.MIN_VALUE;
//            max = Short.MAX_VALUE;
//            break;
//        case JavaTypes.INT:
//            min = Integer.MIN_VALUE;
//            max = Integer.MAX_VALUE;
//            break;
//        case JavaTypes.LONG:
//            min = Long.MIN_VALUE;
//            max = Long.MAX_VALUE;
//            break;
//        default:
//            return false;
//        }
//        return min <= testVal && testVal <= max;
//    }

    /**
     * Perform conversions to make values compatible.
     */
    public static void convertTypes(Value val1, Value val2) {
        Class<?> t1 = val1.getType();
        Class<?> t2 = val2.getType();

        // allow string-to-char conversions
        if (t1 == TYPE_STRING && (Filters.wrap(t2) == TYPE_CHAR_OBJ
            && !(val2 instanceof Path))) {
            val2.setImplicitType(String.class);
            return;
        }
        if (t2 == TYPE_STRING && (Filters.wrap(t1) == TYPE_CHAR_OBJ)
            && !(val1 instanceof Path)) {
            val1.setImplicitType(String.class);
            return;
        }

        // if the non-numeric side is a string of length 1, cast it
        // to a character
        if (t1 == TYPE_STRING && val1 instanceof Literal
            && ((String) ((Literal) val1).getValue()).length() == 1) {
            val1.setImplicitType(Character.class);
            return;
        }
        if (t2 == TYPE_STRING && val2 instanceof Literal
            && ((String) ((Literal) val2).getValue()).length() == 1) {
            val2.setImplicitType(Character.class);
            return;
        }

        // error
        String left;
        String right;
        if (val1 instanceof Path && ((Path) val1).last() != null)
            left = _loc.get("non-numeric-path", ((Path) val1).last().
                getName(), t1.getName()).getMessage();
        else
            left = _loc.get("non-numeric-value", t1.getName()).getMessage();
        if (val2 instanceof Path && ((Path) val2).last() != null)
            right = _loc.get("non-numeric-path", ((Path) val2).last().
                getName(), t2.getName()).getMessage();
        else
            right = _loc.get("non-numeric-value", t2.getName()).getMessage();
        throw new UserException(_loc.get("non-numeric-comparison",
            left, right));
    }

    /**
     * Perform conversions to make values compatible.
     */
    public static void convertTypesQuotedNumbers(Value val1, Value val2) {
        Class<?> t1 = val1.getType();
        Class<?> t2 = val2.getType();

        // if we're comparing to a single-quoted string, convert
        // the value according to the 3.1 rules.
        if (t1 == TYPE_STRING && val1 instanceof Literal
            && ((Literal) val1).getParseType() == Literal.TYPE_SQ_STRING) {
            String s = (String) ((Literal) val1).getValue();
            if (s.length() > 1) {
                ((Literal) val1).setValue(s.substring(0, 1));
                val1.setImplicitType(Character.TYPE);
            }
        }
        if (t2 == TYPE_STRING && val2 instanceof Literal
            && ((Literal) val2).getParseType() == Literal.TYPE_SQ_STRING) {
            String s = (String) ((Literal) val2).getValue();
            if (s.length() > 1) {
                ((Literal) val2).setValue(s.substring(0, 1));
                val2.setImplicitType(Character.TYPE);
            }
        }

        // if we're comparing to a double-quoted string, convert the
        // value directly to a number
        if (t1 == TYPE_STRING && val1 instanceof Literal
            && ((Literal) val1).getParseType() == Literal.TYPE_STRING) {
            String s = (String) ((Literal) val1).getValue();
            ((Literal) val1).setValue(Strings.parse(s, Filters.wrap(t2)));
            val1.setImplicitType(t2);
        }
        if (t2 == TYPE_STRING && val2 instanceof Literal
            && ((Literal) val2).getParseType() == Literal.TYPE_STRING) {
            String s = (String) ((Literal) val2).getValue();
            ((Literal) val2).setValue(Strings.parse(s, Filters.wrap(t1)));
            val2.setImplicitType(t1);
        }
    }

    /**
     * Return true if given class can be used as a number.
     */
    public static boolean isNumeric(Class<?> type) {
        type = Filters.wrap(type);
        return Number.class.isAssignableFrom(type)
            || type == Character.TYPE || type == TYPE_CHAR_OBJ;
    }

    /**
     * Set the implicit types of the given values based on the fact that
     * the first is supposed to contain the second.
     */
    protected void setImplicitContainsTypes(Value val1, Value val2, int op) {
        if (val1.getType() == TYPE_OBJECT) {
            if (op == CONTAINS_TYPE_ELEMENT)
                val1.setImplicitType(Collection.class);
            else
                val1.setImplicitType(Map.class);
        }

        if (val2.getType() == TYPE_OBJECT && val1 instanceof Path) {
            FieldMetaData fmd = ((Path) val1).last();
            ClassMetaData meta;
            if (fmd != null) {
                if (op == CONTAINS_TYPE_ELEMENT || op == CONTAINS_TYPE_VALUE) {
                    val2.setImplicitType(fmd.getElement().getDeclaredType());
                    meta = fmd.getElement().getDeclaredTypeMetaData();
                    if (meta != null) {
                        val2.setMetaData(meta);
                        addAccessPath(meta);
                    }
                } else {
                    val2.setImplicitType(fmd.getKey().getDeclaredType());
                    meta = fmd.getKey().getDeclaredTypeMetaData();
                    if (meta != null) {
                        val2.setMetaData(meta);
                        addAccessPath(meta);
                    }
                }
            }
        }
    }

    /**
     * Set the implicit type of the given value to the given class.
     */
    protected static void setImplicitType(Value val, Class<?> expected) {
        // we never expect a pc type, so no need to worry about metadata
        if (val.getType() == TYPE_OBJECT)
            val.setImplicitType(expected);
    }

    /**
     * Used for obtaining the {@link Localizer} to use for translating
     * error messages.
     */
    protected abstract Localizer getLocalizer();

    /**
     * Returns the current string being parsed; used for error messages.
	 */
	protected abstract String currentQuery ();

    /**
     * Register the schema alias to the current JPQL query context.
     * @param alias
     * @param meta
     */
    protected abstract void addSchemaToContext(String alias,
        ClassMetaData meta);

    /**
     * Register the variable associated with the schema alias (id) to
     * the current JPQL query context.
     * @param id
     * @param var
     */
    protected abstract void addVariableToContext(String id, Value var);

    /**
     * Returns the variable associated with the schema alias (id).
     * @param id
     * @return
     */
    protected abstract Value getVariable(String id);
}

