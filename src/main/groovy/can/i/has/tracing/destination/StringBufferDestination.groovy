package can.i.has.tracing.destination


class StringBufferDestination implements TraceDestination{
    StringBuffer buffer = "" << ""

    @Override
    void trace(String msg) {
        buffer << msg << "\n"
    }
}
