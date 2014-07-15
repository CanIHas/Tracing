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
//        println "registering $method"
        ensureStructure(method.declaringClass.name, method.name)
        def signature = new MethodSignature(method.parameterTypes, method.varArgs)
        def traceOptions = TraceConfig.forAnnotation(method.getAnnotation(Trace))
        registry[method.declaringClass.name][method.name][signature] = traceOptions
    }

    void unregisterMethod(Method method){
//        println "unregistering $method"
        def signature = new MethodSignature(method.parameterTypes, method.varArgs)
        def subregistry = registry[method.declaringClass.name][method.name]
        subregistry.remove(signature)
        if (subregistry.empty)
            registry[method.declaringClass.name].remove(method.name)
    }

    TraceConfig getTraceConfig(Class clazz, String methodName, Object[] arguments){
        def className = clazz.name
        if (! (className in registry.keySet()))
            return null
        if (! (methodName in registry[className]))
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
