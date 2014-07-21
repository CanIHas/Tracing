package can.i.has.tracing

import org.junit.Ignore

@Ignore
class TracerTestCase extends GroovyTestCase{

    //todo: refactor, cleanup
    static void assertEqualTraces(String expected, String actual, Map additionalInfo = null){
        expected = expected.trim()
        actual = actual.trim()
        def expectedLines = expected.split("[\n]")
        def actualLines = actual.split("[\n]")
        def msg = "Traces are of different length!\nExpected: \n${expected}\n--\nActual:\n${actual}\n--\n"
        if (additionalInfo!=null)
            msg += "context: $additionalInfo"
        assertEquals(msg, expectedLines.size(), actualLines.size())
        def differentLines = []
        expectedLines.eachWithIndex { String exp, int i ->
            if (actualLines[i].trim() != exp.trim())
                differentLines.add "${i}\nExpected: \n${exp}\n Actual:\n${actualLines[i].trim()}"
        }
        msg = "Some lines don't match!\n"+differentLines.join("\n")
        if (additionalInfo!=null)
            msg += "\ncontext: $additionalInfo"
        assertTrue(msg, differentLines.empty)
    }
}
