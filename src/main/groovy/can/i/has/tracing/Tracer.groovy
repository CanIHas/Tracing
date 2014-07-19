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

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Commons

import java.lang.reflect.Method

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

    TracingContext withPackageTraced(String pkgName) {
        new TracingContext([pkgName], [])
    }

    TracingContext withPackagesTraced(String... pkgNames){
        new TracingContext(pkgNames as List<String>, [])
    }

    TracingContext withInstanceTraced(instance){
        new TracingContext([], [instance])
    }

    TracingContext withInstancesTraces(Object... instances){
        new TracingContext([], instances as List)
    }


//    public <T> T withPackageTraced(String pkgName, Closure<T> c){
//        registry.registerPackage(pkgName)
//        def out = withTrace(c)
//        registry.unregisterPackage(pkgName)
//        out
//    }
//
//    protected <T> T withTrace(List<ExceptionAwareProxyMetaClass> proxies, Closure<T> c){
////        println "withTrace(proxies: $proxies, c: ${c.toString()})"
//        if (proxies.empty) {
////            println "empty proxies"
//            return c();
//        }
//        ProxyMetaClass proxy = proxies.head()
//        proxy.interceptor = interceptor
//        if (proxies.tail()) {
////            println "proxies.head ${proxies.head()} ||.tail() ${proxies.tail()}"
//            return proxy.use {
//                withTrace(proxies.tail(), c)
//            }
//        }
////        println "proxies.head ${proxies.head()}"
//        proxy.use c
//    }
//
//    protected <T> T withTrace(Closure<T> closure){
//        withTrace(registry.proxyMetaClasses, closure)
//    }

    // consider creating yet another level of nesting: Tracer.TracingContext.State (containing info
    // on all registered packages and methods during this tracing
    @Canonical
    class TracingContext {
        List<String> packages = []
        List instances = []

        TracingContext(List<String> packages, List instances) {
            // hacky way to get rid of duplicates
            this.packages = (packages.findAll() as LinkedHashSet) as List
            this.instances = (instances.findAll() as LinkedHashSet) as List
        }

        TracingContext withPackageTraced(String pkgName) {
            new TracingContext(packages+[pkgName], instances)
        }

        TracingContext withPackagesTraced(String... pkgNames){
            new TracingContext(packages+(pkgNames as List<String>), instances)
        }

        TracingContext withInstanceTraced(instance){
            new TracingContext(packages, instances+[instance])
        }

        TracingContext withInstancesTraces(Object... instances){
            new TracingContext(packages, this.instances+(instances as List))
        }

        public <T> T call(Closure<T> c) {
            packages.each registry.&registerPackage
            T out = withProxies(this.&withInstances.curry(c))
            packages.each registry.&unregisterPackage
            out
        }

        public <T> T _(Closure<T> c) {
            call(c)
        }

        protected <T> T withProxies(Closure<T> c) {
            withProxiesList(registry.proxyMetaClasses, c)
        }

        private <T> T withProxiesList(List<ExceptionAwareProxyMetaClass> proxies, Closure<T> c){
            if (proxies.empty) {
                return c();
            }
            ProxyMetaClass proxy = proxies.head()
            proxy.interceptor = interceptor
            if (proxies.tail()) {
                return proxy.use {
                    withProxiesList(proxies.tail(), c)
                }
            }
            proxy.use c
        }

        protected <T> T withInstances(Closure<T> c) {
            // this is the reason to consider State inner class - we would keep two variables below
            // as class-level variables (in State class) and wouldn't need to pass them later as
            // parameters
            def instanceForcedMethods = registerNeededMethods()
            def originalMetas = changeMetaClasses()
            T out = c()
            restoreMetaClasses(originalMetas)
            unregisterNeededMethods(instanceForcedMethods)
            out
        }

        private List<Method> registerNeededMethods(){
            def out = []
            instances.each { instance ->
                if (!registry.registeredClasses.any {Class clz -> clz.isInstance(instance)}) {
                    instance.class.methods.each { Method m ->
                        if (m.isAnnotationPresent(Trace)) {
                            registry.registerMethod(m)
                            out.add(m)
                        }
                    }
                }
            }
            out
        }

        private void unregisterNeededMethods(List<Method> methods){
            methods.each registry.&unregisterMethod
        }

        /**
         * Used for lightest possible implementation of key-value binding, which would not need keys
         * to be hashable,
         */
        @Canonical
        class Pair<T1, T2> {
            T1 first
            T2 second
        }

        private List<Pair<Object, MetaClass>> changeMetaClasses(){
            def out = []
            instances.each { instance ->
                if (registry.registeredClasses.any {Class clz -> clz.isInstance(instance)}) {
                    //this is ugly, encapsulate it in some method
                    def newMeta = ExceptionAwareProxyMetaClass.getInstance(instance.class)
                    newMeta.interceptor = interceptor
                    def oldMeta = instance.metaClass
                    instance.metaClass = newMeta
                    out.add(new Pair<Object, MetaClass>(instance, oldMeta))
                }
            }
            out
        }

        protected void restoreMetaClasses(List<Pair<Object, MetaClass>> changedMetas) {
            changedMetas.each {
                it.first.metaClass = it.second
            }
        }
    }
}
