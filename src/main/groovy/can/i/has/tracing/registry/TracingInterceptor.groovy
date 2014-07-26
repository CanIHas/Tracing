package can.i.has.tracing.registry

import can.i.has.tracing.destination.TraceDestination
import can.i.has.tracing.events.TracerEventsBus
import can.i.has.tracing.format.TraceFormatter
import can.i.has.tracing.meta.ExceptionAwareInterceptor

import groovy.transform.Canonical

@Canonical
class TracingInterceptor implements ExceptionAwareInterceptor{
    final TraceDestination destination
    final TraceFormatter formatter
    final TracerEventsBus eventBus

    @Override
    Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        def options = TraceTargetRegistry.instance.getTraceConfig(object.class, methodName, arguments)
        if (options) {
            eventBus.fireBeforeOnCall(object, methodName, arguments)
            if (options.onEnter)
                formatter.formatOnCall(object.class, methodName, arguments, options.withArgs).each {
                    destination.trace(it)
                }
//            TraceLevel.instance.enter()
            eventBus.fireAfterOnCall(object, methodName, arguments)
        }
    }

    @Override
    Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        def options = TraceTargetRegistry.instance.getTraceConfig(object.class, methodName, arguments)
        if (options) {
//            TraceLevel.instance.leave()
            eventBus.fireBeforeOnReturn(object, methodName, arguments, result)
            if (options.onReturn)
                formatter.formatOnReturn(object.class, methodName, arguments, result, options.withResult).each {
                    destination.trace(it)
                }
            eventBus.fireAfterOnReturn(object, methodName, arguments, result)
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
//            TraceLevel.instance.leave()
            eventBus.fireBeforeOnThrow(object, methodName, arguments, t)
            if (options.onThrow)
                formatter.formatOnThrow(object.class, methodName, arguments,
                    t, options.withException, options.withStackTrace).each {
                    destination.trace(it)
                }
            eventBus.fireAfterOnThrow(object, methodName, arguments, t)
        }
        true
    }
}
