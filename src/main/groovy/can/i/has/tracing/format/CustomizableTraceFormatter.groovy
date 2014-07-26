package can.i.has.tracing.format

import groovy.transform.Canonical

@Canonical
class CustomizableTraceFormatter implements TraceFormatter {
    /*
    TODO (actually, more like "to think on"):
    - should we add closures for formatting arguments, result and throwable?
    - create formatter builder!
     */

    Closure<String> formatClassName = { Class clazz ->
        clazz.simpleName
    }

    Closure<List<String>> formatStackTrace = buildStackFilterer([], true)

    Closure<Map<String, String>> overrideSeparators = { [:] }

    Closure<String> formatMethodName = {String methodName -> methodName}

    static final Map<String, String> DEFAULT_SEPARATORS = [
        CALLED: "called",
        METHOD: "#",
        WITH_ARGS: "with args:",
        RETURNED: "returned",
        RESULT: "result:",
        THROWED: "throwed",
        THROWABLE: ":"
    ]

    protected Map<String, String> getSeparators(){
        DEFAULT_SEPARATORS + overrideSeparators()
    }


    static final List<String> DEFUALT_IGNORED_TRACE_PREFIXES = [
        "org.codehaus.groovy",
        "sun.reflect",
        "groovy.lang",
        "java.lang",
        "junit",
        "org.junit",
        "can.i.has.tracing.meta.ExceptionAwareProxyMetaClass",
        "can.i.has.tracing.Tracer",
        // I'm developing with IntelliJ IDEA; if anyone want's to colaborate, feel free
        // to add you runners package here too - redundant packages have no negative
        // effect on test outcome or tracing at all
        "com.intellij"
    ]

    static Closure<List<String>> buildStackFilterer(List<String> ignored, boolean addDefaults=true){
        def allIgnored = ignored + (addDefaults ? DEFUALT_IGNORED_TRACE_PREFIXES : [])
        return { List<StackTraceElement> stack ->
            stack.collect {it.toString()}.findAll { String line -> !allIgnored.any {line.startsWith(it)}}
        }
    }

    @Override
    List<String> formatOnCall(Class clazz, String methodName, Object[] args, boolean withArgs) {
        def out = [
            "${formatClassName(clazz)}${separators.METHOD}${formatMethodName(methodName)} ${separators.CALLED}"
        ]
        if (withArgs) {
            def argsLines = "${args as List}".split("\n")
            out[0] += " ${separators.WITH_ARGS} ${argsLines.head()}"
            out.addAll argsLines.tail()
        }
        return out
    }

    @Override
    List<String> formatOnReturn(Class clazz, String methodName, Object[] args, Object result, boolean withResult) {
        def out =  [
            "${formatClassName(clazz)}${separators.METHOD}${formatMethodName(methodName)} ${separators.RETURNED}"
        ]
        if (withResult) {
            def resultLines = "$result".split("\n")
            out[0] += " ${separators.RESULT} ${resultLines.head()}"
            out.addAll resultLines.tail()
        }
        return out
    }

    @Override
    List<String> formatOnThrow(Class clazz, String methodName, Object[] args, Throwable throwable, boolean withThrowable, boolean withStackTrace) {
        def out = [
            "${formatClassName(clazz)}${separators.METHOD}${formatMethodName(methodName)} ${separators.THROWED}"
        ]
        if (withThrowable) {
            def throwableLines = "$throwable".split("\n")
            out[0] += "${separators.THROWABLE} ${throwableLines.head()}"
            out.addAll throwableLines.tail()
        }
        if (withStackTrace)
            out.addAll formatStackTrace(throwable.stackTrace as List<StackTraceElement>)
        return out
    }
}
