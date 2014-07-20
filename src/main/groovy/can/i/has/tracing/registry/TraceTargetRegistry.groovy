package can.i.has.tracing.registry

import eu.infomas.annotation.AnnotationDetector
import can.i.has.tracing.Trace
import can.i.has.tracing.meta.ExceptionAwareProxyMetaClass

import groovy.transform.Canonical
import groovy.util.logging.Commons

import java.lang.reflect.Method

@Singleton
@Canonical
@Commons
class TraceTargetRegistry {

    Map<String, Map<String, Map<MethodSignature, TraceConfig>>> registry = [:]

    void registerMethod(Method method){
        def traceConfig = TraceConfig.forAnnotation(method.getAnnotation(Trace))
        registerMethod(method, traceConfig)
    }

    void registerMethod(Method method, TraceConfig traceConfig) {
//        println "registering $method"
        def signature = new MethodSignature(method.parameterTypes, method.varArgs)
        ensureStructure(method.declaringClass.name, method.name)
        registry[method.declaringClass.name][method.name][signature] = traceConfig
    }

    void unregisterMethod(Method method){
//        println "unregistering $method"
        def signature = new MethodSignature(method.parameterTypes, method.varArgs)
        def classRegistry = registry[method.declaringClass.name]
        def methodRegistry = classRegistry[method.name]
        methodRegistry.remove(signature)
        if (methodRegistry.isEmpty())
            classRegistry.remove(method.name)
        if (classRegistry.isEmpty())
            registry.remove(method.declaringClass.name)
    }

    TraceConfig getTraceConfig(Class clazz, String methodName, Object[] arguments){
        def className = clazz.name
        if (! (className in registry.keySet()))
            return null
        if (! (methodName in registry[className].keySet()))
            return null
        def subregistry = registry[className][methodName]
        def argClasses = arguments.collect {
            it.class
        } as Class[]
        def found = subregistry.keySet().findAll {
            it.matches(argClasses)
        }
        assert found.size()<2
        found ? subregistry[found[0]] : null
    }

    protected void ensureStructure(String clazz, String methodName) {
        if (! (clazz in registry.keySet()))
            registry[clazz] = [:]
        if (! (methodName in registry[clazz].keySet()))
            registry[clazz][methodName] = [:]
    }

    void registerPackage(String packageName){
        new AnnotationDetector(new TraceReporter(TraceReporter.Direction.REGISTER)).detect(packageName)
    }

    void unregisterPackage(String packageName){
        new AnnotationDetector(new TraceReporter(TraceReporter.Direction.UNREGISTER)).detect(packageName)
    }

    List<Class> getRegisteredClasses(){
        registry.keySet().collect {
            Class.forName(it)
        }
    }

    List<ExceptionAwareProxyMetaClass> getProxyMetaClasses(){
        registeredClasses.collect {
            ExceptionAwareProxyMetaClass.getInstance(it)
        }
    }



}
