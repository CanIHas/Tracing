package can.i.has.tracing.destination

//todo: make separator configurable; default to system-wide property; see combination test - there will be some changes
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
