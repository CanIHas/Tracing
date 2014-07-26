package can.i.has.tracing.events


class LevelTrackingListener extends AbstractTraceListener{
    private int lvl = 0

    int getLvl(){
        lvl
    }

    @Override
    void afterOnCall(MethodCalledEvent mce) {
        lvl++
    }

    @Override
    void beforeOnReturn(MethodReturnedEvent mre) {
        lvl--
    }

    @Override
    void beforeOnThrow(MethodThrowedEvent mte) {
        lvl--
    }
}
