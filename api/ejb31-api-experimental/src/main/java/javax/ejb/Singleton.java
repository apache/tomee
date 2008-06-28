package javax.ejb;

@java.lang.annotation.Target(value = {java.lang.annotation.ElementType.TYPE})
@java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Singleton {
    java.lang.String name() default "";

    java.lang.String mappedName() default "";

    java.lang.String description() default "";
}
