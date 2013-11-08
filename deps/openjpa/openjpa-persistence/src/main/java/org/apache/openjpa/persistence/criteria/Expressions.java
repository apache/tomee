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

package org.apache.openjpa.persistence.criteria;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Subquery;
import javax.persistence.criteria.CriteriaBuilder.Trimspec;

import org.apache.openjpa.kernel.exps.ExpressionFactory;
import org.apache.openjpa.kernel.exps.Literal;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.kernel.jpql.JPQLExpressionBuilder;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.criteria.CriteriaExpressionVisitor.TraversalStyle;
import org.apache.openjpa.persistence.meta.Types;

/**
 * Expressions according to JPA 2.0.
 * 
 * A facade to OpenJPA kernel expressions to enforce stronger typing.
 * 
 * @author Pinaki Poddar
 * @author Fay Wang
 * 
 * @since 2.0.0
 *
 */
class Expressions {
    static final String OPEN_BRACE = "(";
    static final String CLOSE_BRACE = ")";
    static final String COMMA = ",";
    
    /**
     * Convert the given Criteria expression to a corresponding kernel value 
     * using the given ExpressionFactory.
     * Handles null expression.
     */
     static Value toValue(ExpressionImpl<?> e, ExpressionFactory factory, CriteriaQueryImpl<?> q) {
        return (e == null) ? factory.getNull() : e.toValue(factory, q);
    }
     
     static void setImplicitTypes(Value v1, Value v2, Class<?> expected, CriteriaQueryImpl<?> q) {
         JPQLExpressionBuilder.setImplicitTypes(v1, v2, expected, q.getMetamodel(), 
             q.getParameterTypes(), q.toString());
     }
     
     /**
      * Visits the given expression and the given children recursively.
      * The order of traversal depends on the parent and is determined by the visitor.
      */
     static void acceptVisit(CriteriaExpressionVisitor visitor, CriteriaExpression parent, Expression<?>...exprs) {
         if (parent == null)
             return;
         TraversalStyle traversal = visitor.getTraversalStyle(parent);
         switch (traversal) {
         case INFIX : 
             if (exprs == null || exprs.length == 0) {
                 visitor.enter(parent);
                 visitor.exit(parent);
                 return;
             }
             for (int i = 0; i < exprs.length; i++) {
                 ExpressionImpl<?> e = (ExpressionImpl<?>)exprs[i];
                 if (e != null) e.acceptVisit(visitor);
                 if (i + 1 != exprs.length) {
                     visitor.enter(parent);
                     visitor.exit(parent);
                 }
             }
             break;
         case POSTFIX:
             visitChildren(visitor,exprs);
             visitor.enter(parent);
             visitor.exit(parent);
             break;
         case PREFIX :
             visitor.enter(parent);
             visitor.exit(parent);
             visitChildren(visitor,exprs);
             break;
         case FUNCTION:
             visitor.enter(parent);
             visitChildren(visitor, exprs);
             visitor.exit(parent);
             break;
         }
     }
     
     static void visitChildren(CriteriaExpressionVisitor visitor, Expression<?>...exprs) {
         for (int i = 0; exprs != null && i < exprs.length; i++) {
             ExpressionImpl<?> e = (ExpressionImpl<?>)exprs[i];
             if (e != null) e.acceptVisit(visitor);
         }
     }
     
     /**
      * Renders the given expressions as a list of values separated by the given connector.
      */
     static StringBuilder asValue(AliasContext q, Expression<?>[] exps, String connector) {
         StringBuilder buffer = new StringBuilder();
         if (exps == null) return buffer;
         for (int i = 0; i < exps.length; i++) {
             buffer.append(((ExpressionImpl<?>)exps[i]).asValue(q));
             if (i+1 != exps.length) {
                 buffer.append(connector);
             }
         }
         return buffer;
     }
     
     /**
      * Renders the given arguments as a list of values separated by the given connector.
      */
     static StringBuilder asValue(AliasContext q, Object...params) {
         StringBuilder buffer = new StringBuilder();
         if (params == null) return buffer;
         for (int i = 0; i < params.length; i++) {
             Object o = params[i];
             if (o == null) {
                 if (i+1 < params.length && params[i+1].equals(COMMA)) {
                     i++;
                 }
                 continue;
             }
             if (o instanceof CriteriaExpression) {
                 buffer.append(((CriteriaExpression)o).asValue(q));
             } else {
                 buffer.append(o);
             }
         }
         return buffer;
     }
     
     /**
      * Return a list that is either empty (if the given list is null) or a list
      * whose mutation do not impact the original list.
      */
     static <X> List<X> returnCopy(List<X> list) {
         return list == null ? new ArrayList<X>() : new CopyOnWriteArrayList<X>(list);
     }
     
     /**
      * Return a set that is either empty (if the given set is null) or a set
      * whose mutation do not impact the original list.
      */
     static <X> Set<X> returnCopy(Set<X> set) {
         return set == null ? new HashSet<X>() : new CopyOnWriteArraySet<X>(set);
     }
     
     static org.apache.openjpa.kernel.exps.Expression and(ExpressionFactory factory,
             org.apache.openjpa.kernel.exps.Expression e1, org.apache.openjpa.kernel.exps.Expression e2) {
             return e1 == null ? e2 : e2 == null ? e1 : factory.and(e1, e2);
     }
         


     /**
     * Unary Functional Expression applies a unary function on a input operand Expression.
     *
     * @param <X> the type of the resultant expression
     */
    public abstract static class UnaryFunctionalExpression<X> extends ExpressionImpl<X> {
        protected final ExpressionImpl<?> e;
        /**
         * Supply the resultant type and input operand expression.
         */
        public UnaryFunctionalExpression(Class<X> t, Expression<?> e) {
            super(t);
            this.e  = (ExpressionImpl<?>)e;
        }
        
        public UnaryFunctionalExpression(Expression<X> e) {
            this((Class<X>)e.getJavaType(), e);
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            Expressions.acceptVisit(visitor, this, e);
        }
    }
    
    /**
     * Binary Functional Expression applies a binary function on a pair of input Expression.
     * 
     * @param <X> the type of the resultant expression
     */
    public abstract static class BinarayFunctionalExpression<X> extends ExpressionImpl<X>{
        protected final ExpressionImpl<?> e1;
        protected final ExpressionImpl<?> e2;
        
        /**
         * Supply the resultant type and pair of input operand expressions.
         */
        public BinarayFunctionalExpression(Class<X> t, Expression<?> x, Expression<?> y) {
            super(t);
            e1 = (ExpressionImpl<?>)x;
            e2 = (ExpressionImpl<?>)y;
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            Expressions.acceptVisit(visitor, this, e1, e2);
        }
    }
    
    /**
     * Functional Expression applies a function on a list of input Expressions.
     * 
     * @param <X> the type of the resultant expression
     */
    public abstract static class FunctionalExpression<X> extends ExpressionImpl<X> {
        protected final ExpressionImpl<?>[] args;
        
        /**
         * Supply the resultant type and list of input operand expressions.
         */
        public FunctionalExpression(Class<X> t, Expression<?>... args) {
            super(t);
            int len = args == null ? 0 : args.length;
            this.args = new ExpressionImpl<?>[len];
            for (int i = 0; args != null && i < args.length; i++) {
                this.args[i] = (ExpressionImpl<?>)args[i];
            }
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            Expressions.acceptVisit(visitor, this, args);
        }
    }
   
    /**
     * Binary Logical Expression applies a function on a pair of input Expression to generate a Predicate
     * i.e. an expression whose resultant type is Boolean.
     *
     */
   public static abstract class BinaryLogicalExpression extends PredicateImpl {
        protected final ExpressionImpl<?> e1;
        protected final ExpressionImpl<?> e2;
        
        public BinaryLogicalExpression(Expression<?> x, Expression<?> y) {
            super();
            e1 = (ExpressionImpl<?>)x;
            e2 = (ExpressionImpl<?>)y;
        }
                
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            Expressions.acceptVisit(visitor, this, e1, e2);
        }
    }
    
    
    public static class Abs<X> extends UnaryFunctionalExpression<X> {
        public  Abs(Expression<X> x) {
            super(x);
        }

        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value value = factory.abs(Expressions.toValue(e, factory, q));
            value.setImplicitType(getJavaType());
            return value;
        }
        
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "ABS", OPEN_BRACE, e, CLOSE_BRACE);
        }
    }
    
    public static class Count extends UnaryFunctionalExpression<Long> {
        private boolean _distinct; 
        public  Count(Expression<?> x) {
            this(x, false);
        }
        
        public  Count(Expression<?> x, boolean distinct) {
            super(Long.class, x);
            _distinct = distinct;
            
        }

        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value v = Expressions.toValue(e, factory, q);
            return _distinct ? factory.count(factory.distinct(v)) : factory.count(v);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "COUNT", OPEN_BRACE, _distinct ? "DISTINCT"+OPEN_BRACE : "", 
                e, _distinct ? CLOSE_BRACE : "", CLOSE_BRACE);
        }
    }

    public static class Avg extends UnaryFunctionalExpression<Double> {
        public  Avg(Expression<?> x) {
            super(Double.class, x);
        }

        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value value = factory.avg(Expressions.toValue(e, factory, q));
            value.setImplicitType(getJavaType());
            return value;
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "AVG", OPEN_BRACE, e, CLOSE_BRACE);
        }
    }
    
    public static class Sqrt extends UnaryFunctionalExpression<Double> {
        public  Sqrt(Expression<? extends Number> x) {
            super(Double.class, x);
        }

        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value value = factory.sqrt(Expressions.toValue(e, factory, q));
            value.setImplicitType(getJavaType());
            return value;
        }
        
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "SQRT", OPEN_BRACE, e, CLOSE_BRACE);
        }
    }
    
    public static class Max<X> extends UnaryFunctionalExpression<X> {
        public  Max(Expression<X> x) {
            super(x);
        }

        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value value = factory.max(Expressions.toValue(e, factory, q));
            value.setImplicitType(getJavaType());
            return value;
        }
        
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "MAX", OPEN_BRACE, e, CLOSE_BRACE);
        }
    }

    public static class Min<X> extends UnaryFunctionalExpression<X> {
        public  Min(Expression<X> x) {
            super(x);
        }

        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value value = factory.min(Expressions.toValue(e, factory, q));
            value.setImplicitType(getJavaType());
            return value;
        }
        
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "MIN", OPEN_BRACE, e, CLOSE_BRACE);
        }
    }
    
    public static class Size extends UnaryFunctionalExpression<Integer> {
        public  Size(Expression<? extends Collection<?>> x) {
            super(Integer.class, x);
        }
        
        public  Size(Collection<?> x) {
            this(new Constant<Collection<?>>(x));
        }

        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value val = Expressions.toValue(e, factory, q);
            Value result;
            if (val instanceof Literal && ((Literal)val).getParseType() == Literal.TYPE_COLLECTION)
                result = factory.newLiteral(((Collection)((Literal)val).getValue()).size(), 
                    Literal.TYPE_NUMBER);
            else
                result = factory.size(val);
            result.setImplicitType(Integer.class);
            return result;
        }
        
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "SIZE", OPEN_BRACE, e, CLOSE_BRACE);
        }
    }
    
    public static class DatabaseFunction<T> extends FunctionalExpression<T> {
        private final String functionName;
        private final Class<T> resultType;
       
        public  DatabaseFunction(String name, Class<T> resultType, Expression<?>... exps) {
            super(resultType, exps);
            functionName = name;
            this.resultType = resultType;
        }
        
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.newFunction(functionName, getJavaType(), 
                new Expressions.ListArgument(resultType, args).toValue(factory, q));
        }
        
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, functionName, OPEN_BRACE, Expressions.asValue(q, args, COMMA), CLOSE_BRACE);
        }
    }

    
    public static class Type<X extends Class> extends UnaryFunctionalExpression<X> {
        public Type(PathImpl<?, ?> path) {
            super((Class)Class.class, path);
        }
        
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.type(Expressions.toValue(e, factory, q));
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "TYPE", OPEN_BRACE, e, CLOSE_BRACE);
        }
    }

    public static class Cast<B> extends UnaryFunctionalExpression<B> {
        public Cast(Expression<?> x, Class<B> b) {
            super(b, x);
        }

        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.cast(Expressions.toValue(e, factory, q), getJavaType());
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, OPEN_BRACE, getJavaType().getSimpleName(), CLOSE_BRACE, e);
        }
    }
    
    public static class Concat extends BinarayFunctionalExpression<String> {
        public Concat(Expression<String> x, Expression<String> y) {
            super(String.class, x, y);
        }
        
        public Concat(Expression<String> x, String y) {
            this(x, new Constant<String>(y));
        }
        
        public Concat(String x, Expression<String> y) {
            this(new Constant<String>(x), y);
        }
        
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.concat(
                Expressions.toValue(e1, factory, q), 
                Expressions.toValue(e2, factory, q));
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "CONCAT", OPEN_BRACE, e1, COMMA, e2, CLOSE_BRACE);
        }
    }
    
    public static class Substring extends UnaryFunctionalExpression<String> {
        private ExpressionImpl<Integer> from;
        private ExpressionImpl<Integer> len;
        
        public Substring(Expression<String> s, Expression<Integer> from, Expression<Integer> len) {
            super(String.class, s);
            this.from = (ExpressionImpl<Integer>)from;
            this.len  = (ExpressionImpl<Integer>)len;
        }
        
        public Substring(Expression<String> s, Expression<Integer> from) {
            this(s, (ExpressionImpl<Integer>)from, null);
        }

        public Substring(Expression<String> s) {
            this(s, (Expression<Integer>)null, (Expression<Integer>)null);
        }
        
        public Substring(Expression<String> s, Integer from) {
            this(s, new Constant<Integer>(from), null);
        }
        
        public Substring(Expression<String> s, Integer from, Integer len) {
            this(s, new Constant<Integer>(from), new Constant<Integer>(len));
        }
        
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return JPQLExpressionBuilder.convertSubstringArguments(factory, 
                Expressions.toValue(e, factory, q), 
                from == null ? null : from.toValue(factory, q), 
                len == null ? null : len.toValue(factory, q));
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            super.acceptVisit(visitor);
            Expressions.acceptVisit(visitor, from, len);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "SUBSTRING", OPEN_BRACE, e, COMMA, from, COMMA, len, CLOSE_BRACE);
        }
    }

    public static class Locate extends ExpressionImpl<Integer> {
        private ExpressionImpl<String> pattern;
        private ExpressionImpl<Integer> from;
        private ExpressionImpl<String> path;
        
        public Locate(Expression<String> path, Expression<String> pattern, Expression<Integer> from) {
            super(Integer.class);
            this.path = (ExpressionImpl<String>)path;
            this.pattern = (ExpressionImpl<String>)pattern;
            this.from = (ExpressionImpl<Integer>)from;
        }

        public Locate(Expression<String> path, Expression<String> pattern) {
            this(path, pattern, null);
         }
        
        public Locate(Expression<String> path, String pattern) {
            this(path, new Constant<String>(pattern), null);
        }
        
        public Locate(String path, Expression<String> pattern) {
            this(new Constant<String>(path), pattern, null);
        }
        
        public Locate(Expression<String> path, String pattern, int from) {
            this(path, new Constant<String>(pattern), new Constant<Integer>(from));
        }

        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value locateSearch = path.toValue(factory, q);
            Value locateFromIndex = (from == null ? null : Expressions.toValue(from, factory, q));
            Value locatePath = Expressions.toValue(pattern, factory, q);

            return factory.indexOf(locateSearch,
                locateFromIndex == null ? locatePath
                    : factory.newArgumentList(locatePath, locateFromIndex));
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            Expressions.acceptVisit(visitor, this, pattern, from, path);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "LOCATE", OPEN_BRACE, pattern, COMMA, path, CLOSE_BRACE);
        }
    }
    
    public static class Trim extends BinarayFunctionalExpression<String> {
        static Expression<Character> defaultTrim = new Constant<Character>(Character.class, Character.valueOf(' '));
        static Trimspec defaultSpec = Trimspec.BOTH;
        private Trimspec ts;
        
        public Trim(Expression<String> x, Expression<Character> y, Trimspec ts) {
            super(String.class, x, y);
            this.ts = ts;
        }
        
        public Trim(Expression<String> x, Expression<Character> y) {
            this(x, y, defaultSpec);
        }
        
        public Trim(Expression<String> x) {
            this(x, defaultTrim, defaultSpec);
        }
        
        public Trim(Expression<String> x, Character t) {
            this(x, new Constant<Character>(Character.class, t), defaultSpec);
        }
        
        public Trim(Expression<String> x, Character t, Trimspec ts) {
            this(x, new Constant<Character>(Character.class, t), ts);
        }
        
        public Trim(Expression<String> x, Trimspec ts) {
            this(x, defaultTrim, ts);
        }

        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Boolean spec = null;
            if (ts != null) {
                switch (ts) {
                case LEADING  : spec = true;  break;
                case TRAILING : spec = false; break;
                case BOTH     : spec = null;  break;
                }
            }
            Character t = (Character)((Constant<Character>)e2).arg;
            Constant<String> e2 = new Constant<String>(String.class, t.toString());
            return factory.trim(
                Expressions.toValue(e1, factory, q), 
                Expressions.toValue(e2, factory, q), spec);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "TRIM", OPEN_BRACE, e1, COMMA, e2, CLOSE_BRACE);
        }        
    }
    
    public static class Sum<N extends Number> extends BinarayFunctionalExpression<N> {
        public Sum(Expression<? extends Number> x, Expression<? extends Number> y) {
            super((Class<N>)x.getJavaType(), x, y);
        }
        
        public Sum(Expression<? extends Number> x) {
            this(x, (Expression<? extends Number>)null);
        }

        public Sum(Expression<? extends Number> x, Number y) {
            this(x, new Constant<Number>(Number.class, y));
        }

        public Sum(Number x, Expression<? extends Number> y) {
            this(new Constant<Number>(Number.class, x), y);
        }
        
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value value= (e2 == null) 
            ?   factory.sum(Expressions.toValue(e1, factory, q))
            :   factory.add(
                   Expressions.toValue(e1, factory, q), 
                   Expressions.toValue(e2, factory, q));
            value.setImplicitType(getJavaType());
            return value;
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return e2 == null 
               ? Expressions.asValue(q, "SUM", OPEN_BRACE, e1, CLOSE_BRACE)
               : Expressions.asValue(q, e1, " + ", e2);
        }        
     }
    
    public static class Product<N extends Number> extends BinarayFunctionalExpression<N> {
        public Product(Expression<? extends Number> x, Expression<? extends Number> y) {
            super((Class<N>)x.getJavaType(), x, y);
        }

        public Product(Expression<? extends Number> x, Number y) {
            this(x, new Constant<Number>(Number.class, y));
        }

        public Product(Number x, Expression<? extends Number> y) {
            this(new Constant<Number>(Number.class, x), y);
        }
        
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.multiply(
                Expressions.toValue(e1, factory, q), 
                Expressions.toValue(e2, factory, q));
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, e1, " * " ,e2);
        }        
    }
    
    public static class Diff<N extends Number> extends BinarayFunctionalExpression<N> {
        public Diff(Expression<? extends Number> x, Expression<? extends Number> y) {
            super((Class<N>)x.getJavaType(), x, y);
        }

        public Diff(Expression<? extends Number> x, Number y) {
            this(x, new Constant<Number>(Number.class, y));
        }

        public Diff(Number x, Expression<? extends Number> y) {
            this(new Constant<Number>(Number.class, x), y);
        }
        
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value value = factory.subtract(
                Expressions.toValue(e1, factory, q), 
                Expressions.toValue(e2, factory, q));
            value.setImplicitType(getJavaType());
            return value;
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, e1, " - " ,e2);
        }        
    }

    
    public static class Quotient<N extends Number> extends BinarayFunctionalExpression<N> {
        public Quotient(Expression<? extends Number> x, Expression<? extends Number> y) {
            super((Class<N>)x.getJavaType(), x, y);
        }

        public Quotient(Expression<? extends Number> x, Number y) {
            this(x, new Constant<Number>(y));
        }

        public Quotient(Number x, Expression<? extends Number> y) {
            this(new Constant<Number>(x), y);
        }
        
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value value = factory.divide(
                Expressions.toValue(e1, factory, q), 
                Expressions.toValue(e2, factory, q));
            value.setImplicitType(getJavaType());
            return value;
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, e1, "%" ,e2);
        }        
    }

    public static class Mod extends BinarayFunctionalExpression<Integer> {
        public  Mod(Expression<Integer> x, Expression<Integer> y) {
            super(Integer.class, x,y);
        }
        public  Mod(Expression<Integer> x, Integer y) {
            this(x,new Constant<Integer>(Integer.class, y));
        }
        public  Mod(Integer x, Expression<Integer> y) {
            this(new Constant<Integer>(Integer.class, x),y);
        }

        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value value = factory.mod(
                Expressions.toValue(e1, factory, q), 
                Expressions.toValue(e2, factory, q));
            value.setImplicitType(getJavaType());
            return value;
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "MOD", OPEN_BRACE, e1, COMMA, e2, CLOSE_BRACE);
        }        
    }

    public static class CurrentDate extends ExpressionImpl<java.sql.Date> {
        public  CurrentDate() {
            super(java.sql.Date.class);
        }

        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.getCurrentDate(getJavaType());
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return new StringBuilder("CURRENT_DATE");
        }
    }
    
    public static class CurrentTime extends ExpressionImpl<java.sql.Time> {
        public  CurrentTime() {
            super(java.sql.Time.class);
        }

        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.getCurrentTime(getJavaType());
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return new StringBuilder("CURRENT_TIME");
        }
    }
    
    public static class CurrentTimestamp extends ExpressionImpl<java.sql.Timestamp> {
        public  CurrentTimestamp() {
            super(java.sql.Timestamp.class);
        }

        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.getCurrentTimestamp(getJavaType());
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return new StringBuilder("CURRENT_TIMESTAMP");
        }
    }

    public static class Equal extends BinaryLogicalExpression {
        public <X,Y> Equal(Expression<X> x, Expression<Y> y) {
            super(x,y);
        }
        
        public <X> Equal(Expression<X> x, Object y) {
            this(x, new Constant(y));
        }

        @Override
        public PredicateImpl not() {
            return new NotEqual(e1, e2).markNegated();
        }
        
        @Override
        org.apache.openjpa.kernel.exps.Expression toKernelExpression(ExpressionFactory factory, CriteriaQueryImpl<?> q){
            Value val1 = Expressions.toValue(e1, factory, q);
            Value val2 = Expressions.toValue(e2, factory, q);
            Expressions.setImplicitTypes(val1, val2, e1.getJavaType(), q);
            return factory.equal(val1, val2);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, e1, " = ", e2);
        }        
    }
    
    public static class NotEqual extends BinaryLogicalExpression {
        public <X,Y> NotEqual(Expression<X> x, Expression<Y> y) {
            super(x,y);
        }
        
        public <X> NotEqual(Expression<X> x, Object y) {
            this(x, new Constant(y));
        }
        
        @Override
        public PredicateImpl not() {
            return new Equal(e1, e2).markNegated();
        }
        
        @Override
        org.apache.openjpa.kernel.exps.Expression toKernelExpression(ExpressionFactory factory, CriteriaQueryImpl<?> q){
            Value val1 = Expressions.toValue(e1, factory, q);
            Value val2 = Expressions.toValue(e2, factory, q);
            Expressions.setImplicitTypes(val1, val2, e1.getJavaType(), q);
            return factory.notEqual(val1, val2);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, e1, " <> ", e2);
        }        
    }
    
    public static class GreaterThan extends BinaryLogicalExpression {
        public <X,Y> GreaterThan(Expression<X> x, Expression<Y> y) {
            super(x,y);
        }
        
        public <X> GreaterThan(Expression<X> x, Object y) {
            this(x, new Constant(y));
        }
        
        @Override
        public PredicateImpl not() {
            return new LessThanEqual(e1, e2).markNegated();
        }
        
        @Override
        org.apache.openjpa.kernel.exps.Expression toKernelExpression(ExpressionFactory factory, CriteriaQueryImpl<?> q){
            Value val1 = Expressions.toValue(e1, factory, q);
            Value val2 = Expressions.toValue(e2, factory, q); 
            Expressions.setImplicitTypes(val1, val2, e1.getJavaType(), q); 
            return factory.greaterThan(val1, val2);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, e1, " > ", e2);
        }        
    }
    
    public static class GreaterThanEqual extends BinaryLogicalExpression {
        public <X,Y> GreaterThanEqual(Expression<X> x, Expression<Y> y) {
            super(x,y);
        }
        
        public <X> GreaterThanEqual(Expression<X> x, Object y) {
            this(x, new Constant(y));
        }
        
        @Override
        public PredicateImpl not() {
            return new LessThan(e1, e2).markNegated();
        }
        
        @Override
        org.apache.openjpa.kernel.exps.Expression toKernelExpression(ExpressionFactory factory, CriteriaQueryImpl<?> q){
            Value val1 = Expressions.toValue(e1, factory, q);
            Value val2 = Expressions.toValue(e2, factory, q); 
            Expressions.setImplicitTypes(val1, val2, e1.getJavaType(), q); 
            return factory.greaterThanEqual(val1, val2);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, e1, " >= ", e2);
        }        
    }
   
    public static class LessThan extends BinaryLogicalExpression {
        public <X,Y> LessThan(Expression<X> x, Expression<Y> y) {
            super(x,y);
        }
        
        public <X> LessThan(Expression<X> x, Object y) {
            this(x, new Constant(y));
        }
        
        @Override
        public PredicateImpl not() {
            return new GreaterThanEqual(e1, e2).markNegated();
        }
        
        @Override
        org.apache.openjpa.kernel.exps.Expression toKernelExpression(ExpressionFactory factory, CriteriaQueryImpl<?> q){
            Value val1 = Expressions.toValue(e1, factory, q);
            Value val2 = Expressions.toValue(e2, factory, q); 
            Expressions.setImplicitTypes(val1, val2, e1.getJavaType(), q); 
            return factory.lessThan(val1, val2);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, e1, " < ", e2);
        }        
    }
    
    public static class LessThanEqual extends BinaryLogicalExpression {
        public <X,Y> LessThanEqual(Expression<X> x, Expression<Y> y) {
            super(x,y);
        }
        
        public <X> LessThanEqual(Expression<X> x, Object y) {
            this(x, new Constant(y));
        }
        
        @Override
        public PredicateImpl not() {
            return new GreaterThan(e1, e2).markNegated();
        }
        
        @Override
        org.apache.openjpa.kernel.exps.Expression toKernelExpression(ExpressionFactory factory, CriteriaQueryImpl<?> q){
            Value val1 = Expressions.toValue(e1, factory, q);
            Value val2 = Expressions.toValue(e2, factory, q); 
            Expressions.setImplicitTypes(val1, val2, e1.getJavaType(), q); 
            return factory.lessThanEqual(val1, val2);
        }
        
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, e1, " <= ", e2);
        }        
    }

    public static class Between<Y extends Comparable<Y>> extends PredicateImpl.And {
        private final ExpressionImpl<? extends Y> e;
        private final ExpressionImpl<? extends Y> v1;
        private final ExpressionImpl<? extends Y> v2;
        
        public Between(Expression<? extends Y> v, Expression<? extends Y> x, Expression<? extends Y> y) {
            super(new GreaterThanEqual(v,x), new LessThanEqual(v,y));
            e = (ExpressionImpl<? extends Y>)v;
            v1 = (ExpressionImpl<? extends Y>)x;
            v2 = (ExpressionImpl<? extends Y>)y;
        }
        
        public Between(Expression<? extends Y> v, Y x, Y y) {
            this(v, new Constant<Y>(x), new Constant<Y>(y));
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, e, " BETWEEN ", v1, " AND ", v2);
        }
    }
    
    public static class Constant<X> extends ExpressionImpl<X> {
        public final Object arg;
        public Constant(Class<X> t, X x) {
            super(t);
            this.arg = x;
        }
        
        public Constant(X x) {
            this(x == null ? null : (Class<X>)x.getClass(), x);
        }
        
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Object value = arg;
            Class<?> literalClass = getJavaType();
            if (arg instanceof ParameterExpressionImpl) {
                return ((ParameterExpressionImpl)arg).toValue(factory, q);
            }
            int literalType = Literal.TYPE_UNKNOWN;
            if (Number.class.isAssignableFrom(literalClass)) {
                literalType = Literal.TYPE_NUMBER;
            } else if (Boolean.class.isAssignableFrom(literalClass)) {
                literalType = Literal.TYPE_BOOLEAN;
            } else if (String.class.isAssignableFrom(literalClass)) {
                literalType = Literal.TYPE_STRING;
            } else if (Enum.class.isAssignableFrom(literalClass)) {
                literalType = Literal.TYPE_ENUM;
            } else if (Class.class.isAssignableFrom(literalClass)) {
                literalType = Literal.TYPE_CLASS;
                Literal lit = factory.newTypeLiteral(value, Literal.TYPE_CLASS);
                ClassMetaData can = ((Types.Entity<X>)q.getRoot().getModel()).meta;
                Class<?> candidate = can.getDescribedType();
                if (candidate.isAssignableFrom((Class)value)) {
                   lit.setMetaData(q.getMetamodel().getRepository().getMetaData((Class<?>)value, null, true));
                } else {
                    lit.setMetaData(can);
                }
                return lit;
            } else if (Collection.class.isAssignableFrom(literalClass)) {
                literalType = Literal.TYPE_COLLECTION;
            }
            return factory.newLiteral(value, literalType);
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            Expressions.acceptVisit(visitor, this, arg instanceof Expression ? ((Expression)arg) : null);
        }
        
        public StringBuilder asValue(AliasContext q) {
            if (arg == null)
                return new StringBuilder("NULL");
            Class<?> literalClass = getJavaType();
            if (arg instanceof ParameterExpressionImpl) {
                return ((ParameterExpressionImpl<?>)arg).asValue(q);
            } else if (Number.class.isAssignableFrom(literalClass)) {
                return new StringBuilder(arg.toString());
            } else if (Boolean.class.isAssignableFrom(literalClass)) {
                return new StringBuilder(arg.toString());
            } else if (String.class.isAssignableFrom(literalClass)) {
                return new StringBuilder("'").append(arg.toString()).append("'");
            } else if (Enum.class.isAssignableFrom(literalClass)) {
                return new StringBuilder(arg.toString());
            } else if (Class.class.isAssignableFrom(literalClass)) {
                return new StringBuilder(((Class)arg).getSimpleName());
            } else if (Collection.class.isAssignableFrom(literalClass)) {
                return new StringBuilder(((Collection)arg).toString());
            }
            return new StringBuilder(arg.toString());
        }
    }
    
    public static class IsEmpty extends PredicateImpl {
        final ExpressionImpl<?> collection;
        public IsEmpty(Expression<?> collection) {
            super();
            this.collection = (ExpressionImpl<?>)collection;
        }
        
        @Override
        public PredicateImpl not() {
            return new IsNotEmpty(collection).markNegated();
        }
        
        @Override
        Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return Expressions.toValue(collection, factory, q);
        }
        
        @Override
        org.apache.openjpa.kernel.exps.Expression toKernelExpression(ExpressionFactory factory, CriteriaQueryImpl<?> q){
            Value val = Expressions.toValue(collection, factory, q);
            return factory.isEmpty(val);
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            super.acceptVisit(visitor);
            Expressions.acceptVisit(visitor, collection);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, collection, " IS EMPTY");
        }
    }
    
    public static class IsNotEmpty extends PredicateImpl {
        final ExpressionImpl<?> collection;
        public IsNotEmpty(Expression<?> collection) {
            super();
            this.collection = (ExpressionImpl<?>)collection;
        }
        
        @Override
        public PredicateImpl not() {
            return new IsEmpty(collection).markNegated();
        }
        
        @Override
        Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return Expressions.toValue(collection, factory, q);
        }
        
        @Override
        org.apache.openjpa.kernel.exps.Expression toKernelExpression(ExpressionFactory factory, CriteriaQueryImpl<?> q){
            Value val = Expressions.toValue(collection, factory, q);
            // factory.isNotEmpty() not used to match JPQL
            return factory.not(factory.isEmpty(val));
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            super.acceptVisit(visitor);
            Expressions.acceptVisit(visitor, collection);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, collection, " IS NOT EMPTY");
        }
    }

    
    public static class Index extends UnaryFunctionalExpression<Integer> {
        public Index(Joins.List<?,?> e) {
            super(Integer.class, e);
        }
        
        @Override
        public org.apache.openjpa.kernel.exps.Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value v = Expressions.toValue(e, factory, q);
            ClassMetaData meta = ((PathImpl<?,?>)e)._member.fmd.getElement().getTypeMetaData();
            v.setMetaData(meta);
            return factory.index(v);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "INDEX", OPEN_BRACE, e, CLOSE_BRACE);
        }
    }
    
    public static class IsMember<E> extends PredicateImpl {
        final ExpressionImpl<E> element;
        final ExpressionImpl<?> collection;
        
        public IsMember(Expression<E> element, Expression<?> collection) {
            super();
            this.element = (ExpressionImpl<E>)element;
            this.collection = (ExpressionImpl<?>)collection;
        }
        
        public IsMember(E element, Expression<?> collection) {
            this(new Constant<E>(element), collection);
        }
        
        @Override
        public org.apache.openjpa.kernel.exps.Expression toKernelExpression(
            ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            org.apache.openjpa.kernel.exps.Expression contains = factory.contains(
                Expressions.toValue(collection, factory, q), 
                Expressions.toValue(element, factory, q));
            return contains;
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            super.acceptVisit(visitor);
            Expressions.acceptVisit(visitor, collection, element);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, element, "MEMBER OF ", collection);
        }
    }
    
    public static class Like extends PredicateImpl {
        public static final String MATCH_MULTICHAR  = "%";
        public static final String MATCH_SINGLECHAR = "_";
        
        final ExpressionImpl<String> str;
        final ExpressionImpl<String> pattern;
        final ExpressionImpl<Character> escapeChar;
        
        public Like(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar) {
            super();
            this.str = (ExpressionImpl<String>)x;
            this.pattern = (ExpressionImpl<String>)pattern;
            this.escapeChar = (ExpressionImpl<Character>)escapeChar;
        }
        
        public Like(Expression<String> x, Expression<String> pat, char esc) {
            this(x, pat, new Constant<Character>(Character.class, esc));
        }
        
        public Like(Expression<String> x, Expression<String> pattern) {
            this(x, pattern, null);
        }
        
        public Like(Expression<String> x, String pattern) {
            this(x, new Constant<String>(pattern), null);
        }
        
        public Like(Expression<String> x, String pat,  
            Expression<Character> esc) {
            this(x, new Constant<String>(pat), esc);
        }
        
        public Like(Expression<String> x, String pat,  Character esc) {
            this(x, new Constant<String>(pat), new Constant<Character>(esc));
        }

        @Override
        public org.apache.openjpa.kernel.exps.Expression toKernelExpression(
            ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            String escapeStr = escapeChar == null ? null :
                ((Character)((Literal)Expressions.toValue(
                    escapeChar, factory, q)).getValue()).toString();
            
            return factory.matches(
                Expressions.toValue(str, factory, q), 
                Expressions.toValue(pattern, factory, q), 
                MATCH_SINGLECHAR, MATCH_MULTICHAR, escapeStr);
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            Expressions.acceptVisit(visitor, this, str, pattern, escapeChar);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, str, " LIKE ", pattern);
        }        
    }
    
    public static class Coalesce<T> extends ExpressionImpl<T> implements CriteriaBuilder.Coalesce<T> {
        private final List<Expression<? extends T>> values = new ArrayList<Expression<? extends T>>();
        
        public Coalesce(Class<T> cls) {
            super(cls);
        }
        
        public Coalesce<T> value(T value) {
            values.add(new Constant<T>(value));
            return this;
        }
        
        public Coalesce<T> value(Expression<? extends T> value) {
            values.add(value); 
            return this;
        }
        
        @Override
        public org.apache.openjpa.kernel.exps.Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value[] vs = new Value[values.size()];
            int i = 0;
            for (Expression<?> e : values)
                vs[i++] = Expressions.toValue((ExpressionImpl<?>)e, factory, q);
            return factory.coalesceExpression(vs);
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            Expressions.acceptVisit(visitor, this, values.toArray(new ExpressionImpl[values.size()]));
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "COALESCE", OPEN_BRACE, Expressions.asValue(q, values == null 
                    ? null : values.toArray(new Expression<?>[values.size()]), COMMA), CLOSE_BRACE);
        }        
    }
    
    public static class Nullif<T> extends ExpressionImpl<T> {
        private Expression<T> val1;
        private Expression<?> val2;

        public Nullif(Expression<T> x, Expression<?> y) {
            super((Class<T>)x.getJavaType());
            val1 = x;
            val2 = y;
        }

        public Nullif(Expression<T> x, T y) {
            this(x, new Constant<T>(y));
        }

        @Override
        public org.apache.openjpa.kernel.exps.Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value value1 = Expressions.toValue((ExpressionImpl<?>)val1, factory, q); 
            Value value2 = Expressions.toValue((ExpressionImpl<?>)val2, factory, q); 
            return factory.nullIfExpression(value1, value2);
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            Expressions.acceptVisit(visitor, this, val1, val2);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "NULLIF", OPEN_BRACE, val1, COMMA, val2, CLOSE_BRACE);
        }        
    }

    public static class IsNull extends PredicateImpl {
        final ExpressionImpl<?> e;
        public IsNull(ExpressionImpl<?> e) {
            super();
            this.e = e;
        }
        
        @Override
        public PredicateImpl not() {
            return new IsNotNull(e).markNegated();
        }
        
        @Override
        org.apache.openjpa.kernel.exps.Expression toKernelExpression(
            ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.equal(
                Expressions.toValue(e, factory, q), 
                factory.getNull());
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            super.acceptVisit(visitor);
            Expressions.acceptVisit(visitor, e);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, e, " IS NULL");
        }
    }
    
    public static class IsNotNull extends PredicateImpl {
        final ExpressionImpl<?> e;
        public IsNotNull(ExpressionImpl<?> e) {
            super();
            this.e = e;
        }
        
        @Override
        public PredicateImpl not() {
            return new IsNull(e).markNegated();
        }
        
        @Override
        org.apache.openjpa.kernel.exps.Expression toKernelExpression(
            ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.notEqual(
                Expressions.toValue(e, factory, q), 
                factory.getNull());
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            super.acceptVisit(visitor);
            Expressions.acceptVisit(visitor, e);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, e, " IS NOT NULL");
        }
    }
    
    
    public static class In<T> extends PredicateImpl.Or implements CriteriaBuilder.In<T> {
        final ExpressionImpl<T> e;
        public In(Expression<?> e) {
            super();
            this.e = (ExpressionImpl<T>)e;
        }
        
        public Expression<T> getExpression() {
            return e;
        }

        public In<T> value(T value) {
            add(new Expressions.Equal(e,value));
            return this;
        }

        public In<T> value(Expression<? extends T> value) {
            add(new Expressions.Equal(e,value));
            return this;
        }
        
        @Override
        public PredicateImpl not() {
            In<T> notIn = new In<T>(e);
            notIn.markNegated();
            for (Predicate e : _exps) {
                notIn.add(e);
            }
            return notIn;
        }
        
        @Override
        org.apache.openjpa.kernel.exps.Expression toKernelExpression(
            ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            org.apache.openjpa.kernel.exps.Expression inExpr = null;
            if (_exps.size() == 1) {
                Expressions.Equal e = (Expressions.Equal)_exps.get(0);
                ExpressionImpl<?> e2 = e.e2;
                ExpressionImpl<?> e1 = e.e1;

                Class<?> e1JavaType = e1.getJavaType();
                Class<?> e2jt = e2.getJavaType();

                // array
                if (BindableParameter.class.isInstance(e2) && BindableParameter.class.cast(e2).value() != null &&
                    ((e2jt.isArray() && e2jt.getComponentType().equals(e1JavaType))
                    || (Class.class.isInstance(e2jt) ||
                        (ParameterizedType.class.isInstance(e2jt)
                            && ParameterizedType.class.cast(e2jt).getActualTypeArguments().length > 0
                            && e1JavaType.equals(ParameterizedType.class.cast(e2jt).getActualTypeArguments()[0]))))) {
                    final BindableParameter bp = BindableParameter.class.cast(e2);
                    final Object value = bp.value();

                    _exps.clear();
                    if (value == null) {
                        add(new Expressions.Equal(e1, null));
                    } else if (value.getClass().isArray()) {
                        final int len = Array.getLength(value);
                        for (int i = 0; i < len; i++) {
                            add(new Expressions.Equal(e1, Array.get(value, i)));
                        }
                    } else if (Collection.class.isInstance(value)) {
                        for (final Object item : Collection.class.cast(value)) {
                            add(new Expressions.Equal(e1, item));
                        }
                    }
                } else {
                    // normal case
                    Value val2 = Expressions.toValue(e2, factory, q);
                    if (!(val2 instanceof Literal)) {
                        Value val1 = Expressions.toValue(e1, factory, q);
                        Expressions.setImplicitTypes(val1, val2, e1.getJavaType(), q);
                        inExpr = factory.contains(val2, val1);
                        return isNegated() ? factory.not(inExpr) : inExpr;
                    } else if (((Literal)val2).getParseType() == Literal.TYPE_COLLECTION) {
                        Collection coll = (Collection)((Literal)val2).getValue();
                        _exps.clear();
                        for (Object v : coll) {
                            add(new Expressions.Equal(e1,v));
                        }
                    }
                }
            } 
            inExpr = super.toKernelExpression(factory, q); 
            IsNotNull notNull = new Expressions.IsNotNull(e);
            
            return factory.and(inExpr, notNull.toKernelExpression(factory, q));
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            super.acceptVisit(visitor);
            Expressions.acceptVisit(visitor, this, e);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            StringBuilder buffer = Expressions.asValue(q, e, " IN ", OPEN_BRACE);
            for (int i = 0; i < _exps.size(); i++) {
                buffer.append(((Equal)_exps.get(i)).e2.asValue(q)).append(i+1 == _exps.size() ? CLOSE_BRACE : COMMA);
            }
            return buffer;
        }        
    }
    
    public static class Case<T> extends ExpressionImpl<T> implements CriteriaBuilder.Case<T> {
        private final List<Expression<? extends T>> thens = new ArrayList<Expression<? extends T>>();
        private final List<Expression<Boolean>> whens = new ArrayList<Expression<Boolean>>();
        private Expression<? extends T> otherwise;

        public Case(Class<T> cls) {
            super(cls);
        }

        public Case<T> when(Expression<Boolean> when, Expression<? extends T> then) {
            whens.add(when);
            thens.add(then);
            return this;
        }

        public Case<T> when(Expression<Boolean> when, T then) {
            return when(when, new Expressions.Constant<T>(then));
        }

        public Case<T> otherwise(Expression<? extends T> otherwise) {
            this.otherwise = otherwise;
            return this;
        }

        public Case<T> otherwise(T otherwise) {
            return otherwise(new Expressions.Constant<T>(otherwise));
        }

        @Override
        public org.apache.openjpa.kernel.exps.Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            int size = whens.size();
            org.apache.openjpa.kernel.exps.Expression[] exps = new org.apache.openjpa.kernel.exps.Expression[size];
            for (int i = 0; i < size; i++) {
                org.apache.openjpa.kernel.exps.Expression expr = 
                    ((ExpressionImpl<?>)whens.get(i)).toKernelExpression(factory, q);
                Value action = Expressions.toValue((ExpressionImpl<?>)thens.get(i), factory, q);
                exps[i] = factory.whenCondition(expr, action);
            }

            Value other = Expressions.toValue((ExpressionImpl<?>)otherwise, factory, q);
            return factory.generalCaseExpression(exps, other);
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            visitor.enter(this);
            for (int i = 0; i < whens.size(); i++) {
                Expressions.visitChildren(visitor, whens.get(i));
                Expressions.visitChildren(visitor, thens.get(i));
            }
            Expressions.visitChildren(visitor, otherwise);
            visitor.exit(this);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            StringBuilder buffer = new StringBuilder("CASE ");
            int size = whens.size();
            for (int i = 0; i < size; i++) {
                buffer.append(Expressions.asValue(q, " WHEN ", whens.get(i), " THEN ", thens.get(i)));
            }
            buffer.append(Expressions.asValue(q, " ELSE ", otherwise, " END"));
            return buffer;
        }
    }

    public static class SimpleCase<C,R> extends ExpressionImpl<R> implements CriteriaBuilder.SimpleCase<C,R> {
        private final List<Expression<? extends R>> thens = new ArrayList<Expression<? extends R>>();
        private final List<Expression<C>> whens = new ArrayList<Expression<C>>();
        private Expression<? extends R> otherwise;
        private Expression<C> caseOperand;

        public SimpleCase(Class<R> cls) {
            super(cls);
        }
        
        public SimpleCase(Expression<C> expr) {
            super(null);
            this.caseOperand = expr;
        }
        
        public Expression<C> getExpression() {
            return caseOperand;
        }

        public SimpleCase<C,R> when(Expression<C> when, Expression<? extends R> then) {
            whens.add(when);
            thens.add(then);
            return this;
        }

        public SimpleCase<C,R> when(C when, Expression<? extends R> then) {
            return when(new Constant<C>(when), then);
        }

        public SimpleCase<C,R> when(C when, R then) {
            return when(when, new Expressions.Constant<R>(then));
        }

        public SimpleCase<C,R> otherwise(Expression<? extends R> otherwise) {
            this.otherwise = otherwise;
            return this;
        }

        public SimpleCase<C,R> otherwise(R otherwise) {
            return otherwise(new Expressions.Constant<R>(otherwise));
        }

        @Override
        public org.apache.openjpa.kernel.exps.Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            Value caseOperandExpr = Expressions.toValue((ExpressionImpl<?>)caseOperand, factory, q);
            int size = whens.size();
            org.apache.openjpa.kernel.exps.Expression[] exps = new org.apache.openjpa.kernel.exps.Expression[size];
            for (int i = 0; i < size; i++) {
                Value when = Expressions.toValue((ExpressionImpl<C>)whens.get(i), factory, q);
                Value action = Expressions.toValue((ExpressionImpl<?>)thens.get(i), factory, q);
                exps[i] = factory.whenScalar(when, action);
            }

            Value other = Expressions.toValue((ExpressionImpl<?>)otherwise, factory, q);
            return factory.simpleCaseExpression(caseOperandExpr, exps, other);
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            visitor.enter(this);
            Expressions.visitChildren(visitor, caseOperand);
            for (int i = 0; i < whens.size(); i++) {
                Expressions.visitChildren(visitor, whens.get(i));
                Expressions.visitChildren(visitor, thens.get(i));
            }
            Expressions.visitChildren(visitor, otherwise);
            visitor.exit(this);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            StringBuilder buffer = new StringBuilder("CASE ");
            int size = whens.size();
            for (int i = 0; i < size; i++) {
                buffer.append(Expressions.asValue(q, " WHEN ", whens.get(i), " THEN ", thens.get(i)));
            }
            buffer.append(Expressions.asValue(q, " ELSE ", otherwise, " END"));
            return buffer;
        }
    }

    public static class Lower extends UnaryFunctionalExpression<String> {
        public Lower(Expression<String> x) {
            super(String.class, x);
        }
        
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.toLowerCase(Expressions.toValue(e, factory, q));
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "LOWER", OPEN_BRACE, e, CLOSE_BRACE);
        }
    }

    public static class Upper extends UnaryFunctionalExpression<String> {
        public Upper(Expression<String> x) {
            super(String.class, x);
        }
        
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.toUpperCase(Expressions.toValue(e, factory, q));
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "UPPER", OPEN_BRACE, e, CLOSE_BRACE);
        }
    }

    public static class Length extends UnaryFunctionalExpression<Integer> {
        public Length(Expression<String> x) {
            super(Integer.class, x);
        }
        
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.stringLength(Expressions.toValue(e, factory, q));
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "LENGTH", OPEN_BRACE, e, CLOSE_BRACE);
        }
    }
    
    public static abstract class SubqueryPredicate<X> extends PredicateImpl {
        final SubqueryImpl<X> e;
        
        public SubqueryPredicate(Subquery<X> x) {
            super();
            e = (SubqueryImpl<X>)x;
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            Expressions.acceptVisit(visitor, this, e);
        }
    }
     
    public static abstract class SubqueryExpression<X> extends ExpressionImpl<X> {
        final SubqueryImpl<X> e;
        
        public SubqueryExpression(Subquery<X> x) {
            super((Class<X>)x.getJavaType());
            e = (SubqueryImpl<X>)x;
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            Expressions.acceptVisit(visitor, this, e);
        }
    }

    public static class Exists<X> extends SubqueryPredicate<X> {
        public Exists(Subquery<X> x) {
            super(x);
        }

        @Override
        org.apache.openjpa.kernel.exps.Expression toKernelExpression(
            ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            org.apache.openjpa.kernel.exps.Expression exists = 
                factory.isNotEmpty(Expressions.toValue(e, factory, q));
            return exists;
        }        
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, " EXISTS", OPEN_BRACE, e, CLOSE_BRACE);
        }
    }
    
    public static class All<X> extends SubqueryExpression<X> {
        public All(Subquery<X> x) {
            super(x);
        }
        
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.all(Expressions.toValue(e, factory, q));
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "ALL", OPEN_BRACE, e, CLOSE_BRACE);
        }
    }

    public static class Any<X> extends SubqueryExpression<X> {
        public Any(Subquery<X> x) {
            super(x);
        }
        
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.any(Expressions.toValue(e, factory, q));
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "ANY", OPEN_BRACE, e, CLOSE_BRACE);
        }
    }

    public static class Not extends PredicateImpl {
        protected final ExpressionImpl<Boolean> e;
        public Not(Expression<Boolean> ne) {
            super();
            e = (ExpressionImpl<Boolean>)ne;
        }
        
        @Override
        public org.apache.openjpa.kernel.exps.Expression toKernelExpression(
          ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            return factory.not(e.toKernelExpression(factory, q));
        }        
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            Expressions.acceptVisit(visitor, this, e);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return Expressions.asValue(q, "NOT ", e);
        }
    }
    
    public static class CastAs<Y> extends ExpressionImpl<Y> {
        protected final ExpressionImpl<?> actual;
        public CastAs(Class<Y> cast, ExpressionImpl<?> actual) {
            super(cast);
            this.actual = actual;
        }
        
        @Override
        public org.apache.openjpa.kernel.exps.Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            org.apache.openjpa.kernel.exps.Value e = actual.toValue(factory, q);
            e.setImplicitType(getJavaType());
            return e;
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            Expressions.acceptVisit(visitor, this, actual);
        }
        
        @Override
        public StringBuilder asValue(AliasContext q) {
            return actual.asValue(q);
        }
    }
    
    /**
     * An expression that is composed of one or more expressions.
     *
     * @param <T>
     */
    public static class ListArgument<T> extends ExpressionImpl<T> {
        private final ExpressionImpl<?>[] _args;
        public ListArgument(Class<T> cls, ExpressionImpl<?>... args) {
            super(cls);
            _args = args;
        }
        
        @Override
        public org.apache.openjpa.kernel.exps.Arguments toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
            org.apache.openjpa.kernel.exps.Value[] kvs = new org.apache.openjpa.kernel.exps.Value[_args.length];
            int i = 0;
            for (ExpressionImpl<?> arg : _args) {
                kvs[i++] = arg.toValue(factory, q);
            }
            org.apache.openjpa.kernel.exps.Arguments e = factory.newArgumentList(kvs);
            e.setImplicitType(getJavaType());
            return e;
        }
        
        public void acceptVisit(CriteriaExpressionVisitor visitor) {
            Expressions.acceptVisit(visitor, this, _args);
        }
    }
}
