package can.i.has.tracing.destination


class StdOutDestination implements TraceDestination{
    @Override
    void trace(String msg) {
        println msg
    }
}
