package can.i.has.tracing

import can.i.has.tracing.destination.StringBufferDestination
import can.i.has.tracing.fixtures.FixtureForCombinations
import can.i.has.tracing.format.CustomizableTraceFormatter
import can.i.has.tracing.format.TraceFormatter
import can.i.has.tracing.registry.TraceConfig
import can.i.has.tracing.registry.TraceTargetRegistry

class ConfigCombinationsTest extends TracerTestCase{
    Tracer tracer
    TraceTargetRegistry registry
    TraceFormatter formatter
    StringBufferDestination destination

    void setUp(){
        registry = TraceTargetRegistry.instance
        formatter = new CustomizableTraceFormatter()

        destination = new StringBufferDestination()
        tracer = new Tracer(formatter, destination)
    }

    List<TraceConfig> getAllConfigs(){
        ([ [true, false] ]* 7).combinations().collect { List<Boolean> args ->
            args as TraceConfig
        }
    }

    void resetDestination(){
        destination.buffer = new StringBuffer()
    }

    //todo: this will change together with StringBufferDestination
    String buildExpectedCallReturn(TraceConfig config, String methodName, def singleArg){
        def out = ""<<""
        def separator = "\n"
        if (config.onEnter)
            formatter.formatOnCall(FixtureForCombinations, methodName, [singleArg] as Object[], config.withArgs).each {
                out << it << "\n"
            }
        if (config.onReturn)
            formatter.formatOnReturn(FixtureForCombinations, methodName, [singleArg] as Object[],
                singleArg, config.withResult).each {
                    out << it << "\n"
                }
        out.toString()
    }

    String buildExpectedCallThrow(TraceConfig config, String methodName, Throwable t){
        def out = ""<<""
        def separator = "\n"
        if (config.onEnter)
            formatter.formatOnCall(FixtureForCombinations, methodName, [] as Object[], config.withArgs).each {
                out << it << "\n"
            }
        if (config.onThrow)
            formatter.formatOnThrow(FixtureForCombinations, methodName, [] as Object[], t,
                config.withException, config.withStackTrace).each {
                out << it << "\n"
            }
        out.toString()
    }



    void testWithPrimitiveTypes(){
        def method = FixtureForCombinations.methods.find {
            it.name=="returningWithPrimitives"
        }
        assert method!=null
        allConfigs.each { TraceConfig config ->
            registry.registerMethod(method, config)

            def expected = buildExpectedCallReturn(config, method.name, 2)
            tracer._ {
                def inst = new FixtureForCombinations()
                inst.returningWithPrimitives(2)
            }

            registry.unregisterMethod(method)

            assertEqualTraces(expected, destination.text, [methodName: method.name, config: config])

            resetDestination()
        }
    }

    void testWithClasses(){
        def method = FixtureForCombinations.methods.find {
            it.name=="returningWithObjects"
        }
        assert method!=null
        allConfigs.each { TraceConfig config ->
            registry.registerMethod(method, config)

            def expected = buildExpectedCallReturn(config, method.name, 2)
            tracer._ {
                def inst = new FixtureForCombinations()
                inst.returningWithObjects(new Integer(2))
            }

            registry.unregisterMethod(method)

            assertEqualTraces(expected, destination.text, [methodName: method.name, config: config])

            resetDestination()
        }
    }

    void testWithThrowing(){
        def method = FixtureForCombinations.methods.find {
            it.name=="throwing"
        }
        assert method!=null
        allConfigs.each { TraceConfig config ->
            registry.registerMethod(method, config)

//            def expected = buildExpectedCallReturn(config, method.name, 2)
            def t
            tracer._ {
                try {
                    def inst = new FixtureForCombinations()
                    inst.throwing()
                } catch (Throwable caught){
                    t = caught
                }
            }
            assert t != null
            def expected = buildExpectedCallThrow(config, method.name, t)
            registry.unregisterMethod(method)

            assertEqualTraces(expected, destination.text, [methodName: method.name, config: config])

            resetDestination()
        }
    }


}
