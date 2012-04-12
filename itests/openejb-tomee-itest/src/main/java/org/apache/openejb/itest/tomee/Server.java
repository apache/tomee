package org.apache.openejb.itest.tomee;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE })
@Retention(RUNTIME)
public @interface Server {
    String name() default ""; // name
    String configurationDir() default "";
    int http() default -1;
    int shutdown() default -1;
    int ajp() default -1;
    boolean cleanWebapp() default true;
    Class<? extends ServerTweaker> tweaker() default org.apache.openejb.itest.tomee.SimpleTweaker.class;
    Artifact artifact() default @org.apache.openejb.itest.tomee.Artifact;
}
