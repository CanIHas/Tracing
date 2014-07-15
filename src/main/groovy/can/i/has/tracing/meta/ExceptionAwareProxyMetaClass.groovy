package can.i.has.tracing.meta

import java.beans.IntrospectionException

/**
 * http://groovy.329449.n5.nabble.com/How-to-handle-exceptions-using-Groovy-Interceptor-tp362732p362739.html
 */
class ExceptionAwareProxyMetaClass extends ProxyMetaClass {
    public static ExceptionAwareProxyMetaClass getInstance(Class theClass) throws IntrospectionException {
        MetaClassRegistry metaRegistry = GroovySystem.getMetaClassRegistry();
        MetaClass meta = metaRegistry.getMetaClass(theClass);
        return new ExceptionAwareProxyMetaClass(metaRegistry, theClass, meta);
    }

    public ExceptionAwareProxyMetaClass(MetaClassRegistry registry, Class theClass, MetaClass adaptee) throws IntrospectionException {
        super(registry, theClass, adaptee);
    }

    public Object invokeMethod(final Object object, final String methodName, final Object[] arguments) {
        return doCall(object, methodName, arguments, interceptor, new Callable() {
            public Object call() {
                return adaptee.invokeMethod(object, methodName, arguments);
            }
        });
    }

    public void setInterceptor(Interceptor interceptor){
        if (!(interceptor instanceof ExceptionAwareInterceptor))
            throw new ClassCastException("Interceptor "+interceptor.toString()+" is not exception aware!");
        super.setInterceptor(interceptor);
    }

    private Object doCall(Object object, String methodName, Object[] arguments, Interceptor interceptor, Callable howToInvoke){
        if (null == interceptor) {
            return howToInvoke.call();
        }
        Object result = interceptor.beforeInvoke(object, methodName, arguments);
        if (interceptor.doInvoke()) {
            try{
                result = howToInvoke.call();
            }
            catch (Throwable t){
                //slightly modified in comparison to original version (see link in class javadoc)
                if(((ExceptionAwareInterceptor)interceptor).onException(object, methodName, arguments, t)) {
                    throw t;
                }
            }
        }
        result = interceptor.afterInvoke(object, methodName, arguments, result);
        return result;
    }

    private interface Callable {
        Object call();
    }
}
