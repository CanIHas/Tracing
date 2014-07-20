package can.i.has.tracing


class TracerTestCase extends GroovyTestCase{
    //todo: refactor, cleanup
    static void assertEqualTraces(String expected, String actual){
        expected = expected.trim()
        actual = actual.trim()
        def expectedLines = expected.split("[\n]")
        def actualLines = actual.split("[\n]")
        assertEquals("Traces are of different length!\nExpected: \n${expected}\n--\nActual:\n${actual}\n--\n", expectedLines.size(), actualLines.size())
        def differentLines = []
        expectedLines.eachWithIndex { String exp, int i ->
            if (actualLines[i].trim() != exp.trim())
                differentLines.add "${i}\nExpected: \n${exp}\n Actual:\n${actualLines[i].trim()}"
        }
        assertTrue("Some lines don't match!\n"+differentLines.join("\n"), differentLines.empty)
    }
}
