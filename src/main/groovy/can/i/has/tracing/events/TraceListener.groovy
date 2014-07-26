package can.i.has.tracing.events

//it should be thread-safe and shouldn't modify event
interface TraceListener {
    void beforeOnCall(MethodCalledEvent mce)
    void beforeOnReturn(MethodReturnedEvent mre)
    void beforeOnThrow(MethodThrowedEvent mte)

    void afterOnCall(MethodCalledEvent mce)
    void afterOnReturn(MethodReturnedEvent mre)
    void afterOnThrow(MethodThrowedEvent mte)
}
