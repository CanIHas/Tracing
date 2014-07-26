package can.i.has.tracing.events

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future


class TracerEventsBus {
    protected ExecutorService threadPool
    protected List<TraceListener> listeners = []

    void start(int poolSize){
        threadPool = Executors.newFixedThreadPool(poolSize)
    }

    void start(double poolSizeToCoreNumberFactor=0.25){
        start(
            [
                (Runtime.runtime.availableProcessors() * poolSizeToCoreNumberFactor) as int,
                1
            ].max()
        )
    }

    void stop(){
        threadPool.shutdown()
    }

    private MethodCalledEvent onCallEvent(Object object, String methodName, Object[] arguments){
        new MethodCalledEvent(new CallArguments(object.class, methodName, arguments))
    }

    private MethodReturnedEvent onReturnEvent(Object object, String methodName, Object[] arguments, Object result){
        new MethodReturnedEvent(new CallArguments(object.class, methodName, arguments), result)
    }

    private MethodThrowedEvent onThrowEvent(Object object, String methodName, Object[] arguments, Throwable t){
        new MethodThrowedEvent(new CallArguments(object.class, methodName, arguments), t)
    }

    private void mapMethod(String methodName, TracerEvent event){
        //todo: timeout would be in place here
        List<Future> futures = listeners.collect { TraceListener listener ->
            threadPool.submit { listener."$methodName"(event) }
        }
        futures.each { it.get() }
    }

    //todo: I suppose it's doable with invokeMethod or methodMissing, but I think it would hurt efficiency
    void fireBeforeOnCall(Object object, String methodName, Object[] arguments){
        mapMethod("beforeOnCall", onCallEvent(object, methodName, arguments))
    }
    void fireBeforeOnReturn(Object object, String methodName, Object[] arguments, Object result){
        mapMethod("beforeOnReturn", onReturnEvent(object, methodName, arguments, result))
    }
    void fireBeforeOnThrow(Object object, String methodName, Object[] arguments, Throwable t){
        mapMethod("beforeOnThrow", onThrowEvent(object, methodName, arguments, t))
    }

    void fireAfterOnCall(Object object, String methodName, Object[] arguments){
        mapMethod("afterOnCall", onCallEvent(object, methodName, arguments))
    }
    void fireAfterOnReturn(Object object, String methodName, Object[] arguments, Object result){
        mapMethod("afterOnReturn", onReturnEvent(object, methodName, arguments, result))
    }
    void fireAfterOnThrow(Object object, String methodName, Object[] arguments, Throwable t){
        mapMethod("afterOnThrow", onThrowEvent(object, methodName, arguments, t))
    }

    void registerListener(TraceListener listener){
        listeners.add listener
    }
    void unregisterListener(TraceListener listener){
        listeners.remove(listener)
    }
    void unregisterAllListeners(){
        listeners.clear()
    }

    List<TraceListener> getListeners(){
        Collections.unmodifiableList(listeners)
    }
}
