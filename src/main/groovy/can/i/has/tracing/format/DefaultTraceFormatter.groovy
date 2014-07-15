package can.i.has.tracing.format

import groovy.transform.Canonical

@Canonical
class DefaultTraceFormatter implements TraceFormatter{
    @Override
    List<String> formatOnCall(Class clazz, String methodName, Object[] args, boolean withArgs) {
        def out = [ "${clazz.simpleName}#$methodName called" ]
        if (withArgs) {
            def argsLines = "${args as List}".split("\n")
            out[0] += " with args: ${argsLines.head()}"
            out.addAll argsLines.tail()
        }
        return out
    }

    @Override
    List<String> formatOnReturn(Class clazz, String methodName, Object[] args,
                                Object result, boolean withResult) {
        def out =  ["${clazz.simpleName}#$methodName returned" ]
        if (withResult) {
            def resultLines = "$result".split("\n")
            out[0] += " result: ${resultLines.head()}"
            out.addAll resultLines.tail()
        }
        return out
    }

    @Override
    List<String> formatOnThrow(Class clazz, String methodName, Object[] args,
                               Throwable throwable, boolean withThrowable, boolean withStackTrace) {
        def out = ["${clazz.simpleName}#$methodName throwed exception" ]
        if (withThrowable) {
            def throwableLines = "$throwable".split("\n")
            out[0] += ": ${throwableLines.head()}"
            out.addAll throwableLines.tail()
        }
        if (withStackTrace)
            out.addAll throwable.stackTrace.collect {StackTraceElement ste ->
                "$ste"
            }
        return out
    }
}
