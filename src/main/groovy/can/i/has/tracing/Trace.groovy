package can.i.has.tracing

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Trace {
    boolean onEnter() default true
    boolean withArgs() default true
    boolean onReturn() default true
    boolean withResult() default true
    boolean onThrow() default true
    boolean withException() default true
    boolean withStackTrace() default false
}
