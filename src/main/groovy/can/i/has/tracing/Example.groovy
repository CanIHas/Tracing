package can.i.has.tracing

import can.i.has.tracing.destination.StringBufferDestination

Tracer.GLOBAL = new Tracer(
    Tracer.DEFAULT.formatter,
    new StringBufferDestination(),
    Tracer.DEFAULT.eventBus.listeners
)

Tracer.GLOBAL.withPackageTraced("can.i.has")._ {
    def x = new TracedClass1()
    x.method("a")
    x.method("a", 5)
    x.method("a", "b")
    x.recursive(5)
    x.untraced()
    try {
//        println "trying"
    x.raisin()
//        println "tried"
    } catch (Throwable ignored) {}//{println "caught $ignored"; ignored.printStackTrace()}
}

println Tracer.GLOBAL.destination.buffer.toString()