/*
 * Unless otherwise stated, this program in binary or source code
 * format and any accompanying media/files/resources are
 * Copyright 2009 Kunye Mining Solutions
 * 
 * Contact Details
 * ------------------------------------------------
 *   Web Site: http://www.kunye.net
 *  Telephone: +27 12 807 3590/1
 *        Fax: +27 12 807 3592
 *       Cell: +27 82 855 5907
 *     E-Mail: akruger@kunye.net
 * 
 * Postal Address
 * ------------------------------------------------
 *  P.O. Box 70032
 *  Die Wilgers
 *  Pretoria
 *  0041
 */
package org.apache.openejb.junit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author quintin
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestSecurity {
    public static final String UNAUTHENTICATED = "";

    /**
     * List of roles for which the test should succeed.
     */
    String[] authorized() default {};

    /**
     * List of roles for which the test should fail with an EJBAccessException
     */
    String[] unauthorized() default {};
}
