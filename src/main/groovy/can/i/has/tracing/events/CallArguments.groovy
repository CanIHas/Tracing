package can.i.has.tracing.events

import groovy.transform.Canonical

@Canonical
class CallArguments {
    final Class clazz
    final String methodName
    final Object[] args
}
