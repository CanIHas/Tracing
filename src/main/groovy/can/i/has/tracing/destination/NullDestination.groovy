package can.i.has.tracing.destination


class NullDestination implements TraceDestination{
    @Override
    void trace(String msg) {}
}
