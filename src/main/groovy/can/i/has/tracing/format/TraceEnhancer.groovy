package can.i.has.tracing.format

import groovy.transform.Canonical

@Canonical
abstract class TraceEnhancer implements TraceFormatter{
    TraceFormatter delegate

    @Override
    List<String> formatOnCall(Class clazz, String methodName, Object[] args, boolean withArgs) {
        enhance(delegate.formatOnCall(clazz, methodName, args, withArgs))
    }

    @Override
    List<String> formatOnReturn(Class clazz, String methodName, Object[] args,
                                Object result, boolean withResult) {
        enhance(delegate.formatOnReturn(clazz, methodName, args, result, withResult))
    }

    @Override
    List<String> formatOnThrow(Class clazz, String methodName, Object[] args,
                               Throwable throwable, boolean withThrowable, boolean withStackTrace) {
        enhance(delegate.formatOnThrow(clazz, methodName, args, throwable,
            withThrowable, withStackTrace))
    }

    abstract String enhance(String msg)

    List<String> enhance(List<String> msgs){
        msgs.collect {
            enhance(it)
        }
    }

    static TraceFormatter chain (TraceFormatter formatter, TraceEnhancer... enhancers){
        def result = formatter
        enhancers.each {
            it.delegate = result
            result = it
        }
        result
    }
}
