package can.i.has.tracing


class TracedClass1 {
    @Trace
    def method(String a, int b=2){
        return "$a $b"
    }

    @Trace(withResult = false)
    def method(String a, String b){
        return "$a -- $b"
    }

    def untraced(){
        "whateva"
    }

    @Trace
    def recursive(int i = 0){
        assert i>=0
        if (i>0)
            recursive(i-1)
        return i
    }

    @Trace(withStackTrace = true)
    def raisin(){
        recursive(-1)

    }
}
