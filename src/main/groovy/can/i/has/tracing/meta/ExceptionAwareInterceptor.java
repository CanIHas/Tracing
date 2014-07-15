package can.i.has.tracing.meta;


import groovy.lang.Interceptor;

/**
 * http://groovy.329449.n5.nabble.com/How-to-handle-exceptions-using-Groovy-Interceptor-tp362732p362739.html
 */
public interface ExceptionAwareInterceptor extends Interceptor {
    /**
     * This code is called if intercepted method throwed an exception.
     *
     * @param object        receiver object for the method call
     * @param methodName    name of the method to call
     * @param arguments     arguments to the method call
     * @param t             The exception that has been thrown in the invocation of method being intercepted
     * @return              true if the exception should be propogated, else false
     */
    //slightly modified in comparison to original version (see link in class javadoc)
    boolean onException(Object object, String methodName, Object[] arguments, Throwable t);
}

