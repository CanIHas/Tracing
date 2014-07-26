package can.i.has.tracing.events

import groovy.transform.Canonical

@Canonical
class MethodThrowedEvent implements TracerEvent{
    final CallArguments callArguments
    final Throwable throwable
}
