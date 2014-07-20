package can.i.has.tracing

import can.i.has.tracing.destination.FileTraceDestination
import can.i.has.tracing.destination.StdOutDestination
import can.i.has.tracing.destination.StringBufferDestination
import can.i.has.tracing.fixtures.Clazz1
import can.i.has.tracing.format.DefaultTraceFormatter
import can.i.has.tracing.format.TraceEnhancer
import can.i.has.tracing.format.TraceLevel


class TracerFixturesTest extends TracerTestCase{
    Clazz1 clazz1
    Tracer tracer
    String expected1
    String failingFactorial
    String plainFactorial
    String longFactorial

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
        expected1 = getFixture("fixtures/expected1.txt")
        failingFactorial = getFixture("fixtures/failingFactorial.txt")
        plainFactorial = getFixture("fixtures/plainFactorial.txt")
        longFactorial = getFixture("fixtures/longFactorial.txt")
        clazz1 = new Clazz1()
    }

    String getFixture(String path){
        this.class.classLoader.getResource(path).text.trim()
    }

    void testStandardUseCaseWithPackage(){
        tracer.withPackageTraced("can.i.has.tracing.fixtures")._ {
            clazz1 = new Clazz1()
            clazz1.foo(2, 6)
        }
        def actual = tracer.destination.text
        assertEqualTraces(expected1, actual)
    }

    //todo: consider swapping meta to saved.delegate (since wrapped with org.codehaus.groovy.runtime.HandleMetaClass)
    void testStandardUseCaseWithInstance(){
        tracer.withInstanceTraced(clazz1)({
            clazz1.foo(2, 6)
        })
        def actual = tracer.destination.text
        assertEqualTraces(expected1, actual)
    }

    void testFailingFactorial(){
        tracer.withInstanceTraced(clazz1)._ {
                try {
                    clazz1.factorial(-1)
                } catch (Throwable ignored) {}
            }
        def actual = tracer.destination.text
        assertEqualTraces(failingFactorial, actual)
    }

    void testPlainFactorial(){
        tracer.withInstanceTraced(clazz1)._ {
                clazz1.factorial(0) == clazz1.factorial(1)
            }
        def actual = tracer.destination.text
        assertEqualTraces(plainFactorial, actual)
    }

    void testLongFactorial(){
        tracer.withInstanceTraced(clazz1)._ {
                clazz1.factorial(8)
            }
        def actual = tracer.destination.text
        assertEqualTraces(longFactorial, actual)
    }
}
