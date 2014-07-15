package can.i.has.tracing

def proxy = ProxyMetaClass.getInstance(CLS)
proxy.interceptor = new Interceptor() {
    @Override
    Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        println ">>> $object $methodName $arguments"
    }

    @Override
    Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        result
    }

    @Override
    boolean doInvoke() {
        true
    }
}
proxy.use {
    def c = new CLS()
    c.method("a")
}

def methods = CLS.methods.findAll {
    it.name == "method"
}.each {
    println "$it"
    println "${it.defaultValue}"
    println "${it.annotations as List}"

}

print "="*80

(ClassLoader.systemClassLoader as URLClassLoader).URLs.each {
    println it
}
