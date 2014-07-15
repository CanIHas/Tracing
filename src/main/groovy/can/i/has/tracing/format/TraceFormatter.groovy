package can.i.has.tracing.format


interface TraceFormatter {
    List<String> formatOnCall(Class clazz, String methodName, Object[] args, boolean withArgs)
    List<String> formatOnReturn(Class clazz, String methodName, Object[] args,
                                Object result, boolean withResult)
    List<String> formatOnThrow(Class clazz, String methodName, Object[] args,
                               Throwable throwable, boolean withThrowable, boolean withStackTrace)
}
