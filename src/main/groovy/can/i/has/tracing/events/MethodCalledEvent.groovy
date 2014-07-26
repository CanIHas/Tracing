package can.i.has.tracing.events

import groovy.transform.Canonical

@Canonical
class MethodCalledEvent implements TracerEvent{
    final CallArguments callArguments
}
