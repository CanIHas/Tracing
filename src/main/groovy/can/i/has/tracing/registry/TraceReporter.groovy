package can.i.has.tracing.registry

import can.i.has.tracing.Trace
import eu.infomas.annotation.AnnotationDetector

import groovy.transform.Canonical
import groovy.util.logging.Commons

import java.lang.annotation.Annotation
import java.lang.reflect.Method

@Canonical
@Commons
class TraceReporter implements AnnotationDetector.MethodReporter{
    enum Direction {
        REGISTER,
        UNREGISTER
    }

    final Direction direction

    @Override
    Class<? extends Annotation>[] annotations() {
        [Trace] as Class[]
    }

    @Override
    void reportMethodAnnotation(Class<? extends Annotation> annotation, String className, String methodName) {
        Class.forName(className).methods.findAll { Method m ->
            if (m.name == methodName && m.isAnnotationPresent(Trace)) {
                switch (direction) {
                    case Direction.REGISTER: TraceTargetRegistry.instance.registerMethod(m); break;
                    case Direction.UNREGISTER: TraceTargetRegistry.instance.unregisterMethod(m); break;
                    default: throw new IllegalStateException("Unknown reporting direction!")
                }
            }
        }
    }
}
