package can.i.has.tracing.registry

import groovy.transform.Canonical

@Canonical
final class TraceConfig {
    boolean onEnter
    boolean withArgs
    boolean onReturn
    boolean withResult
    boolean onThrow
    boolean withException
    boolean withStackTrace

    private TraceConfig(boolean onEnter, boolean withArgs,
                boolean onReturn, boolean withResult,
                boolean onThrow, boolean withException, boolean withStackTrace) {
        this.onEnter = onEnter
        this.withArgs = withArgs
        this.onReturn = onReturn
        this.withResult = withResult
        this.onThrow = onThrow
        this.withException = withException
        this.withStackTrace = withStackTrace
    }

    static TraceConfig forAnnotation(annotation){
        new TraceConfig(
            annotation.onEnter(), annotation.withArgs(),
            annotation.onReturn(), annotation.withResult(),
            annotation.onThrow(), annotation.withException(),
            annotation.withStackTrace()
        )
    }
}
