package can.i.has.tracing

import can.i.has.tracing.destination.FileTraceDestination
import can.i.has.tracing.destination.StringBufferDestination
import can.i.has.tracing.format.DateEnhancer
import can.i.has.tracing.format.DefaultTraceFormatter
import can.i.has.tracing.format.TraceEnhancer
import can.i.has.tracing.format.TraceLevel

Tracer.GLOBAL = new Tracer(
    TraceEnhancer.chain(
        new DefaultTraceFormatter(),
        new TraceLevel.IndentEnhancer(),
        new TraceLevel.Enhancer(),
        new DateEnhancer()
    ),
//    new StringBufferDestination()
    new FileTraceDestination()
)

Tracer.GLOBAL.withPackageTraced("can.i.has") {
    def x = new TracedClass1()
//    x.method("a")
//    x.method("a", 5)
//    x.method("a", "b")
//    x.recursive(5)
//    x.untraced()
    try {
    x.raisin()
    } catch (Throwable ignored) {}
}

//println Tracer.GLOBAL.destination.buffer.toString()