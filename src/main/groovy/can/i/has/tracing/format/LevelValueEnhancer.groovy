package can.i.has.tracing.format

import can.i.has.tracing.events.LevelTrackingListener

import groovy.transform.Canonical

@Canonical
class LevelValueEnhancer extends TraceEnhancer{
    final LevelTrackingListener listener
    final String leftBracket = "["
    final String rightBracket = "]"

    @Override
    String enhance(String msg) {
        "$leftBracket${listener.lvl}$rightBracket $msg"
    }
}
