package can.i.has.tracing.destination


class StringBufferDestination implements TraceDestination{
    @Delegate StringBuffer buffer = "" << ""


    @Override
    void trace(String msg) {
        buffer << msg << "\n"
    }

    String getText(){
        buffer.toString().trim()
    }
}
