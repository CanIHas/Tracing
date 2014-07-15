package can.i.has.tracing

import can.i.has.tracing.destination.CommonsTraceDestination
import can.i.has.tracing.destination.TraceDestination
import can.i.has.tracing.format.DefaultTraceFormatter
import can.i.has.tracing.format.TraceEnhancer
import can.i.has.tracing.format.TraceFormatter
import can.i.has.tracing.format.TraceLevel
import can.i.has.tracing.meta.ExceptionAwareProxyMetaClass
import can.i.has.tracing.registry.TraceTargetRegistry
import can.i.has.tracing.registry.TracingInterceptor

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class Tracer {
    final static Tracer DEFAULT = new Tracer(
        TraceEnhancer.chain(
            new DefaultTraceFormatter(),
            new TraceLevel.IndentEnhancer()),
        new CommonsTraceDestination()
    )
    static Tracer GLOBAL = DEFAULT

    final TraceFormatter formatter
    final TraceDestination destination
    protected final TracingInterceptor interceptor
    final TraceTargetRegistry registry = TraceTargetRegistry.instance

    Tracer(TraceFormatter formatter, TraceDestination destination) {
        this.formatter = formatter
        this.destination = destination
        this.interceptor = new TracingInterceptor(destination, formatter)
    }

    public <T> T withPackageTraced(String pkgName, Closure<T> c){
        registry.registerPackage(pkgName)
        def out = withTrace(c)
        registry.unregisterPackage(pkgName)
        out
    }

    protected <T> T withTrace(List<ExceptionAwareProxyMetaClass> proxies, Closure<T> c){
//        println "withTrace(proxies: $proxies, c: $c)"
        if (proxies.empty) {
//            println "empty proxies"
            return c();
        }
        if (proxies.tail()) {
//            println "proxies.head ${proxies.head()} ||.tail() ${proxies.tail()}"
            ProxyMetaClass proxy = proxies.head()
            proxy.interceptor = interceptor
            return proxy.use {
                withTrace(proxies.tail(), c)
            }
        }
//        println "proxies.head ${proxies.head()}"
        proxies.head().use c
    }

    protected <T> T withTrace(Closure<T> closure){
        withTrace(registry.proxyMetaClasses, closure)
    }
}
