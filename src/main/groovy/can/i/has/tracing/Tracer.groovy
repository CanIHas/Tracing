package can.i.has.tracing

import can.i.has.tracing.destination.Slf4jTraceDestination
import can.i.has.tracing.destination.TraceDestination
import can.i.has.tracing.events.LevelTrackingListener
import can.i.has.tracing.events.TraceListener
import can.i.has.tracing.events.TracerEventsBus
import can.i.has.tracing.format.CustomizableTraceFormatter
import can.i.has.tracing.format.LevelValueEnhancer
import can.i.has.tracing.format.TraceEnhancer
import can.i.has.tracing.format.TraceFormatter
import can.i.has.tracing.meta.ExceptionAwareProxyMetaClass
import can.i.has.tracing.registry.TraceTargetRegistry
import can.i.has.tracing.registry.TracingInterceptor

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.lang.reflect.Method

//todo: tracing context should allow pool size customization (and timeouts in future?)
@ToString
@EqualsAndHashCode
class Tracer {
    final static Tracer DEFAULT = getDefaultTracer()
    static Tracer GLOBAL = DEFAULT

    private static Tracer getDefaultTracer(){
        def levelListener = new LevelTrackingListener()
        new Tracer(
            TraceEnhancer.chain(
                new CustomizableTraceFormatter(),
                new LevelValueEnhancer(levelListener)
            ),
            new Slf4jTraceDestination(),
            [levelListener]
        )
    }

    final TraceFormatter formatter
    final TraceDestination destination
    protected final TracerEventsBus eventBus
    protected final TracingInterceptor interceptor
    final TraceTargetRegistry registry = TraceTargetRegistry.instance

    Tracer(TraceFormatter formatter, TraceDestination destination, List<TraceListener> listeners = []) {
        this.formatter = formatter
        this.destination = destination
        this.eventBus = new TracerEventsBus()
        this.interceptor = new TracingInterceptor(destination, formatter, eventBus)
        listeners.each eventBus.&registerListener

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

    public <T> T call(Closure<T> c) {
        new TracingContext([], []).call c
    }

    public <T> T _(Closure<T> c) {
        call c
    }

    public <T> T leftShift(Closure<T> c){
        call c
    }

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
            eventBus.start()
            packages.each registry.&registerPackage
            T out = withProxies(this.&withInstances.curry(c))
            packages.each registry.&unregisterPackage
            eventBus.stop()
            out
        }

        public <T> T _(Closure<T> c) {
            call c
        }

        public <T> T leftShift(Closure<T> c){
            call c
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
                //this should be findAll, and we should find highest commons supertype
                def instancesClass = registry.registeredClasses.find {Class clz -> clz.isInstance(instance)}
                if (instancesClass!=null && !(instance.class.metaClass instanceof ExceptionAwareProxyMetaClass)) {
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
