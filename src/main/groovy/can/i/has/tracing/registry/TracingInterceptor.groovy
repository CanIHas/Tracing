package can.i.has.tracing.registry

import can.i.has.tracing.destination.TraceDestination
import can.i.has.tracing.format.TraceFormatter
import can.i.has.tracing.format.TraceLevel
import can.i.has.tracing.meta.ExceptionAwareInterceptor

import groovy.transform.Canonical

@Canonical
class TracingInterceptor implements ExceptionAwareInterceptor{
    TraceDestination destination
    TraceFormatter formatter

    @Override
    Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        def options = TraceTargetRegistry.instance.getTraceConfig(object.class, methodName, arguments)
        if (options) {
            formatter.formatOnCall(object.class, methodName, arguments, options.withArgs).each {
                destination.trace(it)
            }
            TraceLevel.instance.enter()
        }
    }

    @Override
    Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        def options = TraceTargetRegistry.instance.getTraceConfig(object.class, methodName, arguments)
        if (options) {
            TraceLevel.instance.leave()
            formatter.formatOnReturn(object.class, methodName, arguments, result, options.withResult).each {
                destination.trace(it)
            }
        }
        result

    }

    @Override
    boolean doInvoke() {
        true
    }

    @Override
    boolean onException(Object object, String methodName, Object[] arguments, Throwable t) {
        def options = TraceTargetRegistry.instance.getTraceConfig(object.class, methodName, arguments)
        if (options) {
            TraceLevel.instance.leave()
            formatter.formatOnThrow(object.class, methodName, arguments,
                t, options.withException, options.withStackTrace).each {
                destination.trace(it)
            }

        }
        true
    }
}
