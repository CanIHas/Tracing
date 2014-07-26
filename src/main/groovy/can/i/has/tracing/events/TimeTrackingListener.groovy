package can.i.has.tracing.events


class TimeTrackingListener extends AbstractTraceListener{
    private List<Long> timeStack = []
    private long lastDuration

    long getLastDuration(){
        lastDuration
    }

    @Override
    void beforeOnCall(MethodCalledEvent mce) {
        timeStack.add System.currentTimeMillis()
    }

    @Override
    void beforeOnReturn(MethodReturnedEvent mre) {
        lastDuration = System.currentTimeMillis()-timeStack.pop()
    }

    @Override
    void beforeOnThrow(MethodThrowedEvent mte) {
        lastDuration = System.currentTimeMillis()-timeStack.pop()
    }
}
