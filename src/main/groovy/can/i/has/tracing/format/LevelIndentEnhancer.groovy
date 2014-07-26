package can.i.has.tracing.format

import can.i.has.tracing.events.LevelTrackingListener

import groovy.transform.Canonical

@Canonical
class LevelIndentEnhancer extends TraceEnhancer{
    final LevelTrackingListener listener
    final String indent = "  "

    @Override
    String enhance(String msg) {
        "${indent*(listener.lvl)}$msg"
    }
}
