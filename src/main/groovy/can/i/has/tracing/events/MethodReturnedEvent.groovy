package can.i.has.tracing.events

import groovy.transform.Canonical

@Canonical
class MethodReturnedEvent implements TracerEvent{
    final CallArguments callArguments
    final Object result
}
