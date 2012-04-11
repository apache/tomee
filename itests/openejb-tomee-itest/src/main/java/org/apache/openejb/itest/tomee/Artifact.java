package org.apache.openejb.itest.tomee;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE })
@Retention(RUNTIME)
public @interface Artifact {
    String groupId() default "org.apache.openejb";
    String artifactId() default "apache-tomee";
    String version() default "1.0.0-beta-3-SNAPSHOT";
    String classifier() default "webprofile";
    String type() default "zip";
}
