package can.i.has.tracing

import can.i.has.tracing.destination.FileTraceDestination
import can.i.has.tracing.destination.StdOutDestination
import can.i.has.tracing.destination.StringBufferDestination
import can.i.has.tracing.fixtures.Clazz1
import can.i.has.tracing.format.DefaultTraceFormatter
import can.i.has.tracing.format.TraceEnhancer
import can.i.has.tracing.format.TraceLevel


class TracerPackagesTest extends GroovyTestCase {
    Clazz1 clazz1
    Tracer tracer
    String expected1

    void setUp(){
        tracer = new Tracer(
            TraceEnhancer.chain(
                new DefaultTraceFormatter(),
                new TraceLevel.Enhancer()
            ),
//            new StdOutDestination()
//            new FileTraceDestination()
            new StringBufferDestination()
        )
        def url = this.class.classLoader.getResource("fixtures/expected1.txt")
        expected1 = url.text.trim()
    }

    void testStandardUseCaseWithPackage(){
        tracer.withPackageTraced("can.i.has.tracing.fixtures")._ {
            clazz1 = new Clazz1()
            clazz1.foo(2, 6)
        }
        StringBufferDestination dest = tracer.destination
        def buffer = dest.buffer
        def actual = buffer.toString().trim()
        assertEquals(expected1, actual)
    }

    //todo: consider swapping meta to saved.delegate (since wrapped with org.codehaus.groovy.runtime.HandleMetaClass)
    void testStandardUseCaseWithInstance(){
        clazz1 = new Clazz1()
        tracer.withInstanceTraced(clazz1).
            _ {
            clazz1.foo(2, 6)
        }
        StringBufferDestination dest = tracer.destination
        def buffer = dest.buffer
        def actual = buffer.toString().trim()
        assertEquals(expected1, actual)
    }
}
