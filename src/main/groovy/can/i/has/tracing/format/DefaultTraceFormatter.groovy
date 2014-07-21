package can.i.has.tracing.format

import groovy.transform.Canonical

@Canonical
class DefaultTraceFormatter implements TraceFormatter{

    // I'm developing with IntelliJ IDEA; if anyone want's to colaborate, feel free
    // to add you runners package here too - redundant packages have no negative
    // effect of test outcome

    List<String> ignoredPackages = [
        "org.codehaus.groovy",
        "sun.reflect",
        "groovy.lang",
        "java.lang",
        "junit",
        "org.junit",
        "com.intellij"
    ]


    String className(Class clazz){
        clazz.simpleName
    }

    @Override
    List<String> formatOnCall(Class clazz, String methodName, Object[] args, boolean withArgs) {
        def out = [ "${className(clazz)}#$methodName called" ]
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
        def out =  ["${className(clazz)}#$methodName returned" ]
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
        def out = ["${className(clazz)}#$methodName throwed exception" ]
        if (withThrowable) {
            def throwableLines = "$throwable".split("\n")
            out[0] += ": ${throwableLines.head()}"
            out.addAll throwableLines.tail()
        }
        if (withStackTrace)
            out.addAll formatStackTrace(throwable.stackTrace as List<StackTraceElement>)
//            out.addAll throwable.stackTrace.collect {StackTraceElement ste ->
//                "$ste"
//            }
        return out
    }

    List<String> formatStackTrace(List<StackTraceElement> stack){
        stack.
            collect { "$it" }.
            findAll { String line -> !ignoredPackages.any { line.startsWith(it) }}
    }
}
