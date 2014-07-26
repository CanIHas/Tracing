package can.i.has.tracing.events


abstract class AbstractTraceListener implements TraceListener{
    @Override
    void beforeOnCall(MethodCalledEvent mce) {}

    @Override
    void beforeOnReturn(MethodReturnedEvent mre) {}

    @Override
    void beforeOnThrow(MethodThrowedEvent mte) {}

    @Override
    void afterOnCall(MethodCalledEvent mce) {}

    @Override
    void afterOnReturn(MethodReturnedEvent mre) {}

    @Override
    void afterOnThrow(MethodThrowedEvent mte) {}
}
