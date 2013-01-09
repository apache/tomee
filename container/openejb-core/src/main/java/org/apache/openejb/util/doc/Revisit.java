package org.apache.openejb.util.doc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

/**
 * @version $Rev$ $Date$
 */
@Retention(RetentionPolicy.SOURCE)
@Target({CONSTRUCTOR, FIELD, METHOD, TYPE, ANNOTATION_TYPE, LOCAL_VARIABLE, PACKAGE, PARAMETER})
public @interface Revisit {
    String value();
}
