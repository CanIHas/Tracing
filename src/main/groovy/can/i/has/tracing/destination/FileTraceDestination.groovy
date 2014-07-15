package can.i.has.tracing.destination

import groovy.transform.Canonical

@Canonical
class FileTraceDestination implements TraceDestination{
    File file = new File("./trace.log")

    @Override
    void trace(String msg) {
        if (!file.exists())
            file.text = ""
        file.append("$msg\n")
    }
}
