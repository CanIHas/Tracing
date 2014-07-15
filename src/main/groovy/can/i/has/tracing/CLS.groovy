package can.i.has.tracing


class CLS {
    @Trace
    def method(String a, int b=2){}

    @Trace
    def method(String a, String b){}
}
